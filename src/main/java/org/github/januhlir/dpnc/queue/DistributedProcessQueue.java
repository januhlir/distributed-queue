package org.github.januhlir.dpnc.queue;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Distributed Processing Queue based on simple socket server and one thread per
 * client proxy. This solution is not well suited for very high number of
 * clients, soon threads and connetions resources would run out. To improve
 * this, NIO would have to be used, to lower number of needed threads; and UDP
 * stateless connections.
 * 
 * Technically unlimited number of participating remote clients; they can be
 * added or dropped anytime.
 * 
 * Client using this queue is expected to implement {@link ClientProxyProcess}
 * to encode and decode data directly to/from stream.
 * 
 * Queue interface was limited to just to basic (blocking) operations:
 * {@link #put(Object)} and {@link #take()}.
 * 
 * @author Jan.Uhlir
 *
 * @param <IT>
 *            input type
 * @param <OT>
 *            output type
 */
public class DistributedProcessQueue<IT, OT> {
	int MAX_BUFFERED_INPUTS = 100;
	private final BlockingQueue<IT> inputQueue = new LinkedBlockingQueue<IT>(MAX_BUFFERED_INPUTS);
	private final BlockingQueue<OT> outputQueue = new LinkedBlockingQueue<OT>();
	private final ClientProxyProcess<IT, OT> workerProcess;
	private final ServerSocketRunnable serverSocketRunnable;
	
	public DistributedProcessQueue(ClientProxyProcess<IT, OT> workerProcess, int serverPortNumber) {
		this.workerProcess = workerProcess;
		serverSocketRunnable = new ServerSocketRunnable(serverPortNumber);
	}
	
	public void start() {
		System.out.println("Starting DistributedProcessQueue");
		new Thread(serverSocketRunnable).start();
	}
	
	/**
	 * Equivalent of {@link BlockingQueue#put(Object)}
	 */
	public void put(IT e) throws InterruptedException {
		// when MAX_BUFFERED_INPUTS is reached caller will be blocked
		inputQueue.put(e);
	}
	
	/**
	 * Equivalent of {@link BlockingQueue#take()}
	 */
	public OT take() throws InterruptedException {
		return outputQueue.take();
	}
	
	class ServerSocketRunnable implements Runnable {
		private final int serverPortNumber;
		
		public ServerSocketRunnable(int serverPortNumber) {
			this.serverPortNumber = serverPortNumber;
		}

		@Override
		public void run() {
			ServerSocket serverSocket = null;
			Socket socket = null;
			try {
				serverSocket = new ServerSocket(serverPortNumber);
				while (true) {
					socket = serverSocket.accept();
					new Thread(new ClientProxy<IT, OT>(socket, workerProcess, inputQueue, outputQueue)).start();
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			} finally {
				System.out.println("Closing DistributedProcessQueue");
				try {
					if (serverSocket != null) serverSocket.close();
				} catch (IOException e) {
					System.out.println("Exception when closing ServerSocket");
				}
			}
		}
	}
}
