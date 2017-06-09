package org.github.januhlir.dpnc.queue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
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
class ClientProxy<IT, OT> {
	private final SocketChannel channel;
	private final ClientProxyProcess<IT, OT> clientProxyProcess;

	private final BlockingQueue<IT> inputQueue;
	private final BlockingQueue<OT> outputQueue;

	public ClientProxy(
			SocketChannel channel, 
			ClientProxyProcess<IT, OT> workerProcess,
			BlockingQueue<IT> inputQueue,
			BlockingQueue<OT> outputQueue
			) {
		this.channel = channel;
		this.inputQueue = inputQueue;
		this.outputQueue = outputQueue;
		this.clientProxyProcess = workerProcess;
	}

	// ------------------------------------------

	private boolean unfinishedWrite = false;
	private boolean unfinishedRead = false;

	private ByteBuffer wBuff;
	private ByteBuffer rBuff;

	private final byte[] internalReadBuffer = new byte[4 + 1];

	void write() throws InterruptedException {
		if (!unfinishedWrite) { 
			IT input = inputQueue.take();

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(baos);

			clientProxyProcess.write(out, input);

			try {
				// TODO: why array wrapping does not work?
				//wBuff = ByteBuffer.wrap(baos.toByteArray());
				wBuff = ByteBuffer.allocate(4 + 1);
				wBuff.clear();
				for (byte b : baos.toByteArray()) {
					wBuff.put(b);
				}
				wBuff.flip(); // prepare buffer for channel data send out

				channel.write(wBuff);
				if (!wBuff.hasRemaining()) {
					unfinishedWrite = false;
				} else {
					unfinishedWrite = true;
				}

			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else {
			// unfinished write from previous IO event
			// try to finish it now
			// write buffer remembers position where it ended last time
			try {
				int r = channel.write(wBuff);
				if (r == -1) {
					unfinishedWrite = false;
				} else {
					unfinishedWrite = true;
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	// TODO: refactor!
	void read() throws InterruptedException {
		if (!unfinishedRead) {
			try {
				byte rFrameSize = readSizeOfTheResponse();
				if (rFrameSize < 1) {
					// either channel was closed or no data at all arrived yet
					unfinishedRead = false;
					return; 
				}

				rBuff = ByteBuffer.allocate(rFrameSize);
				rBuff.clear();  // prepare buffer for channel data receive

				channel.read(rBuff);
				if (!rBuff.hasRemaining()) {
					unfinishedRead = false;
				} else {
					unfinishedRead = true;
					return; // if unfinished immediately return to the main loop
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

			rBuff.flip(); // prepare buffer for reading
			rBuff.get(internalReadBuffer);
			ByteArrayInputStream bais = new ByteArrayInputStream(internalReadBuffer);
			DataInputStream in = new DataInputStream(bais);

			OT result = clientProxyProcess.read(in);
			outputQueue.put(result);
		} else {
			// unfinished read from previous IO event
			// try to finish it now
			// read buffer remembers position where it ended last time
			try {
				channel.read(rBuff);
				if (!rBuff.hasRemaining()) {
					unfinishedRead = false;
				} else {
					unfinishedRead = true;
					return; // if unfinished immediately return to the main loop
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

			rBuff.flip(); // prepare buffer for reading
			rBuff.get(internalReadBuffer);
			ByteArrayInputStream bais = new ByteArrayInputStream(internalReadBuffer);
			DataInputStream in = new DataInputStream(bais);

			OT result = clientProxyProcess.read(in);
			outputQueue.put(result);
		}	
	}

	private byte readSizeOfTheResponse() throws IOException {
		ByteBuffer buff = ByteBuffer.allocate(1);
		buff.clear();  // prepare buffer for channel data receive
		int r = channel.read(buff);
		if (r < 1) {
			return -1;
		}
		buff.flip();
		return buff.get();
	}

	void close() {
		try {
			channel.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean isUnfinishedWrite() {
		return unfinishedWrite;
	}

	public boolean isUnfinishedRead() {
		return unfinishedRead;
	}
}