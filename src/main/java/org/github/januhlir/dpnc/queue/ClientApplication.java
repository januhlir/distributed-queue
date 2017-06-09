package org.github.januhlir.dpnc.queue;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * This class is supposed to be extended by a participating remote client. It
 * encapsulates all technicalities of connecting to the server and waiting for
 * inputs to be processed. Client application then just need to implement
 * template method {@link #process(DataInputStream, DataOutputStream)}
 * 
 * Note: distributed queue server has to be already running on the given port. 
 * 
 * @author Jan.Uhlir
 */
public abstract class ClientApplication {
	private final ClientSocketRunnable clientSocketRunnable;

	public ClientApplication(int serverPortNumber) {
		clientSocketRunnable = new ClientSocketRunnable(serverPortNumber);
	}

	/**
	 * Implementor should decode inputs from input stream, process inputs and
	 * put encoded outputs to output stream.
	 * 
	 * @param in
	 * @param out
	 * @throws InterruptedException
	 */
	public abstract void process(DataInputStream in, DataOutputStream out) throws InterruptedException;

	public void run() {
		new Thread(clientSocketRunnable).start();
	}

	class ClientSocketRunnable implements Runnable {
		private final Socket socket;
		private final DataInputStream in;
		private final DataOutputStream out;

		ClientSocketRunnable(int serverPortNumber) {
			String hostName = "localhost";
			try {
				this.socket = new Socket(hostName, serverPortNumber);
				this.in = new DataInputStream(socket.getInputStream());
				this.out = new DataOutputStream(socket.getOutputStream());
			} catch (UnknownHostException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public void run() {
			try {
				while (true) {
					try {
						ByteArrayOutputStream baos = new ByteArrayOutputStream(10);
						DataOutputStream outBuff = new DataOutputStream(baos);
						
						process(in, outBuff);
						
						out.writeByte(baos.size()); // send frame size first 
						out.write(baos.toByteArray());
						out.flush();
					} catch (IOException e) {
						throw new RuntimeException(e);
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
}
