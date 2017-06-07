package org.github.januhlir.dpnc;

import org.github.januhlir.dpnc.prime.PrimeApplication;
import org.github.januhlir.dpnc.prime.PrimeApplicationRun;
import org.github.januhlir.dpnc.randomizer.RandomizerApplication;
import org.github.januhlir.dpnc.randomizer.RandomizerApplicationRun;

/**
 * Run 1 Randomizer server, {@link RandomizerApplication} and 5 Prime
 * distributed processing queue clients, {@link PrimeApplication}.
 * 
 * This is full multi-JVM example. It expects java executable to be found on
 * path. It is environmentally more fragile so I also provide similar setup, but
 * all inside one JVM, see {@link RunTestSetup}.
 * 
 * Known issue: this process, if terminated, leaves child process running
 * (orphan processes). There is no simple solution for that in pure Java.
 * This issue cause failure of subsequent run because port is still binded.
 * 
 * @author Jan.Uhlir
 */
public class RunTestMultiJvmSetup {
	
	public static void main(String[] args) {
		
		// run server (spawn JVM)
		runJava(RandomizerApplicationRun.class);

		// run 5 clients (spawns 5 JVMs)
		runJava(PrimeApplicationRun.class);
		runJava(PrimeApplicationRun.class);
		runJava(PrimeApplicationRun.class);
		runJava(PrimeApplicationRun.class);
		runJava(PrimeApplicationRun.class);
	}
	
	private static void runJava(Class<?> classToRun) {
		new Thread(() -> {
			runJava_(classToRun);
		}).start();
	}

	private static void runJava_(Class<?> classToRun) {
		String classpath = System.getProperty("java.class.path");
		try {
			ProcessBuilder processBuilder = new ProcessBuilder(
				"java",
				"-cp", classpath,
				classToRun.getName())
			.inheritIO();
			Process process = processBuilder.start();
			process.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	
}
