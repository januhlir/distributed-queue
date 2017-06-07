package org.github.januhlir.dpnc.prime;

import org.github.januhlir.dpnc.randomizer.RandomizerApplication;

/**
 * Convenient, simple runner wrapper for {@link RandomizerApplication}. 
 * TODO: add argument handling, enable setting of server port, etc.. 
 */
public class PrimeApplicationRun {
	private static final int SERVER_PORT = 15678;

	public static void main(String[] args) {
		System.out.println("Starting Prime client connecting to port " + SERVER_PORT);
		new PrimeApplication(SERVER_PORT).run();
	}
}
