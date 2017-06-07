package org.github.januhlir.dpnc;

import org.github.januhlir.dpnc.prime.PrimeApplication;
import org.github.januhlir.dpnc.prime.PrimeApplicationRun;
import org.github.januhlir.dpnc.randomizer.RandomizerApplication;
import org.github.januhlir.dpnc.randomizer.RandomizerApplicationRun;

/**
 * Run 1 Randomizer server, {@link RandomizerApplication} and 5 Prime
 * distributed processing queue clients, {@link PrimeApplication}.
 * 
 * All inside one JVM. For full multi-JVM example run 
 * {@link RunTestMultiJvmSetup}.
 * 
 * @author Jan.Uhlir
 */
public class RunTestSetup {
	public static void main(String[] args) {
		// run server (in-process)
		RandomizerApplicationRun.main(args);
		
		// run 5 clients (all also in-process)
		PrimeApplicationRun.main(args);
		PrimeApplicationRun.main(args);
		PrimeApplicationRun.main(args);
		PrimeApplicationRun.main(args);
		PrimeApplicationRun.main(args);
	}
}
