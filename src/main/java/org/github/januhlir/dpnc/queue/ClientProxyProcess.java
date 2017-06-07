package org.github.januhlir.dpnc.queue;

import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Core, specialised part of server client proxy, {@link ClientProxy}, concerned
 * with encoding and decoding of parameters and waiting for response.
 * 
 * NOTE: ideally this should be split into multiple classes, but for simplicity
 * sake and this assignment I am keeping this in one class.
 * 
 * @author Jan.Uhlir
 *
 * @param <IT>
 *            input type
 * @param <OT>
 *            output type
 */
public interface ClientProxyProcess<IT, OT> {

	/**
	 * Implementor should encode inputs and place them to output stream, then
	 * wait for result to appear on the input stream. When appears, decode and
	 * return in required type.
	 * 
	 * @param out
	 * @param in
	 * @throws InterruptedException
	 */
	OT process(
			DataOutputStream out,
			DataInputStream in,
			IT input);
}