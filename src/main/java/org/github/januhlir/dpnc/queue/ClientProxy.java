package org.github.januhlir.dpnc.queue;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

/**
 * This class encapsulate technicalities of socket communication between server
 * client proxy and client itself. Writing to IO streams and waiting for
 * response is implemented in attached {@link ClientProxyProcess}
 * instance.
 * 
 * @author Jan.Uhlir
 *
 * @param <IT>
 *            input type
 * @param <OT>
 *            output type
 */
class ClientProxy<IT, OT> implements Runnable {
	private final Socket socket;
	private final DataInputStream in;
	private final DataOutputStream out;
	private final ClientProxyProcess<IT, OT> clientProxyProcess;

	private final BlockingQueue<IT> inputQueue;
	private final BlockingQueue<OT> outputQueue;

	public ClientProxy(
			Socket socket, 
			ClientProxyProcess<IT, OT> workerProcess,
			BlockingQueue<IT> inputQueue,
			BlockingQueue<OT> outputQueue
			) {
		this.socket = socket;
		try {
			this.in = new DataInputStream(socket.getInputStream());
			this.out = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		this.inputQueue = inputQueue;
		this.outputQueue = outputQueue;
		this.clientProxyProcess = workerProcess;
	}

	/**
	 * Take a request from the internal input queue, sends it to
	 * remote client, waiting for response, when place result to the internal
	 * output queue.
	 */
	public void run() {
		try {
			while (true) {
				try {
					IT input = inputQueue.take();
					OT result = clientProxyProcess.process(out, in, input);
					outputQueue.put(result);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					return;
				}
			}
		} finally {
			close();
		}
	}

	private void close() {
		try {
			socket.close();
			in.close();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}