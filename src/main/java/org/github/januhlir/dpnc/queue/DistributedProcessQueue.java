package org.github.januhlir.dpnc.queue;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Distributed Processing Queue based on NIO socket channel enabling to use just
 * one thread serving all client proxies. This solution is well suited for very
 * high number of clients.
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
	int MAX_BUFFERED_INPUTS = 5;
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

	// -----------------------------------------------------------

	class ServerSocketRunnable implements Runnable {
		private final int serverPortNumber;

		public ServerSocketRunnable(int serverPortNumber) {
			this.serverPortNumber = serverPortNumber;
		}

		private final Map<SocketChannel, ClientProxy<IT, OT>> clients = new HashMap<>();

		@Override
		public void run() {
			ServerSocketChannel ssc = null;
			try {
				// Selector for incoming requests
				Selector selector = SelectorProvider.provider().openSelector();

				// Create a new server socket and set to non blocking mode
				ssc = ServerSocketChannel.open();
				ssc.configureBlocking(false);

				// Bind the server socket to the local host and port
				//InetSocketAddress isa = new InetSocketAddress(InetAddress.getLocalHost(), serverPortNumber);
				InetSocketAddress isa = new InetSocketAddress(serverPortNumber);
				ssc.socket().bind(isa);
				
				// register for accept events
				// server socket channel is registered only for OP_ACCEPT
				// resulting client socket channels are later registered for OP_WRITE and OP_READ
				ssc.register(selector, SelectionKey.OP_ACCEPT);

				while (selector.select() > 0) {  
					// if there is no data event (no registered data event) then execution will 
					// be blocked on select() call

					// Someone is ready for I/O, get the ready keys
					Set<SelectionKey> readyKeys = selector.selectedKeys();
					Iterator<SelectionKey> keyIterator = readyKeys.iterator();

					// Walk through the ready keys collection and process date requests.
					while (keyIterator.hasNext()) {
						SelectionKey key = (SelectionKey)keyIterator.next();
						keyIterator.remove();

						if (key.isAcceptable()) {
							ServerSocketChannel readyServerSocketChannel = (ServerSocketChannel)key.channel();

							// Accept the date request and send back the date string
							SocketChannel clientSocketChannel = readyServerSocketChannel.accept();
							clientSocketChannel.configureBlocking(false);
							clientSocketChannel.register(selector, SelectionKey.OP_WRITE);

							// register new ClientProxy on successful accept()
							clients.put(clientSocketChannel, new ClientProxy<IT, OT>(clientSocketChannel, workerProcess, inputQueue, outputQueue));
						}
						else if (key.isWritable()) {
							SocketChannel clientSocketChannel = (SocketChannel) key.channel();
							ClientProxy<IT, OT> clientProxy = clients.get(clientSocketChannel);
							clientProxy.write();

							if (!clientProxy.isUnfinishedWrite()) {
								// when writing to socket is completed, switch channel to "reading mode"
								clientSocketChannel.register(selector, SelectionKey.OP_READ);
							}
						}
						else if (key.isReadable()) {
							SocketChannel clientSocketChannel = (SocketChannel) key.channel();
							ClientProxy<IT, OT> clientProxy = clients.get(clientSocketChannel);
							clientProxy.read();

							if (!clientProxy.isUnfinishedRead()) {
								// when reading from socket is completed, switch channel to "writing mode"
								clientSocketChannel.register(selector, SelectionKey.OP_WRITE);
							}
						}
					}
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return;
			} catch (IOException e) {
				throw new RuntimeException(e);
			}  finally {
				System.out.println("Closing DistributedProcessQueue");
				try {
					if (ssc != null) ssc.close();
				} catch (IOException e) {
					System.out.println("Exception when closing ServerSocket");
				}
			}
		}
	}
}
