package org.github.januhlir.dpnc.randomizer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Random;

import org.github.januhlir.dpnc.queue.ClientProxyProcess;
import org.github.januhlir.dpnc.queue.DistributedProcessQueue;

public class RandomizerApplication {
	private DistributedProcessQueue<Integer, PrimeResult> queue;
	public static final int MAX_RANDOM_NUMBER = Integer.MAX_VALUE;
	
	public RandomizerApplication() {
		this(15678);
	}
	
	public RandomizerApplication(int randomizerPortNumber) {
		queue = new DistributedProcessQueue<>(this::process, randomizerPortNumber);
	}
	
	public void run() {
		new Thread(new Generator()).start();
		new Thread(new Receiver()).start();
		queue.start();
	}
	
	/**
	 * Generate inputs for distributed Prime checkers
	 */
	class Generator implements Runnable {
		private final Random randomGenerator = new Random();
		
		@Override
		public void run() {
			while (true) {
				int randomInt = Math.abs(randomGenerator.nextInt(MAX_RANDOM_NUMBER));
				try {
					queue.put(randomInt);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					return;
				}
			}
		}
	}

	/** 
	 * Receive results from Prime checkers and print them 
	 */
	class Receiver implements Runnable {
		@Override
		public void run() {
			while (true) {
				try {
					PrimeResult r = queue.take();
					if (r.isPrime()) {
						System.out.println(String.format("Number %d is a prime number", r.getNumber()));
					} else {
						System.out.println(String.format("Number %d is NOT prime number", r.getNumber()));
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					return;
				}
			}
		}
	}

	/**
	 * See {@link ClientProxyProcess#process(DataOutputStream, DataInputStream, Object)}.
	 */
	public PrimeResult process(
			DataOutputStream out,
			DataInputStream in,
			Integer input) {
		try {
			out.writeInt(input);

			int number = in.readInt(); // wait until response arrives
			if (number != input) {
				throw new RuntimeException("verification check failed"); // FIXME: improve
			}
			boolean isPrime = in.readBoolean();

			return new PrimeResult(number, isPrime);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
