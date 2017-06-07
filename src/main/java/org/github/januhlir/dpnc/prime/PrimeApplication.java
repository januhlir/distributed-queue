package org.github.januhlir.dpnc.prime;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.github.januhlir.dpnc.queue.ClientApplication;

/**
 * Specialization of distributed queue's {@link ClientApplication}
 * delegating to {@link PrimeNumberCheck}.
 * 
 * @author Jan.Uhlir
 */
public class PrimeApplication extends ClientApplication {
	
	public PrimeApplication(int serverPortNumber) {
		super(serverPortNumber);
	}

	private final PrimeNumberCheck pnc = new PrimeNumberCheckSimple();


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(DataInputStream in, DataOutputStream out) throws InterruptedException {
		try {
			int testedNumber = in.readInt();
			boolean isPrime = pnc.isPrime(testedNumber);
			out.writeInt(testedNumber);
			out.writeBoolean(isPrime);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}		
	}
}
