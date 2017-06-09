package org.github.januhlir.dpnc.queue;

import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Core, specialised part of server client proxy, {@link ClientProxy}, concerned
 * with encoding and decoding of parameters.
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
	 * Implementor should encode inputs from input stream. Caller will ensure,
	 * presumably by buffering, that all data frame is ready in full to be
	 * decoded
	 * 
	 * @param out
	 * @param input
	 */
	OT read(
			DataInputStream in);
	
	/**
	 * Implementor should encode outputs and place them to output stream.
	 * 
	 * @param out
	 * @param input
	 */
	void write(
			DataOutputStream out,
			IT input);
}