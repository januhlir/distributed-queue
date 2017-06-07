package org.github.januhlir.prime;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Set;
import java.util.TreeSet;

import org.github.januhlir.dpnc.prime.PrimeNumberCheckSimple;
import org.junit.Test;

public class PrimeNumberCheckSimpleTest {
	// Prime numbers up to 100 are
	// Source: http://planetmath.org/howtofindwhetheragivennumberisprimeornot
	private int[] basicPrimes = new int[]{2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97};
	private Set<Integer> basicPrimesSet = new TreeSet<Integer>();
	{
		for (int i = 0; i < basicPrimes.length; i++) {
			basicPrimesSet.add(basicPrimes[i]);
		}
	}

	private PrimeNumberCheckSimple pnc = new PrimeNumberCheckSimple();

	@Test
	public void testAllUnder10Primes() {
		assertFalse("0 is not a prime number", pnc.isPrime(0));
		assertFalse("1 is not a prime number", pnc.isPrime(1));
		assertTrue( "2 is a prime number",     pnc.isPrime(2));
		assertTrue( "3 is a prime number",     pnc.isPrime(3));
		assertFalse("4 is not a prime number", pnc.isPrime(4));
		assertTrue( "5 is a prime number",     pnc.isPrime(5));
		assertFalse("6 is not a prime number", pnc.isPrime(6));
		assertTrue( "7 is a prime number",     pnc.isPrime(7));
		assertFalse("8 is not a prime number", pnc.isPrime(8));
		assertFalse("9 is a prime number",     pnc.isPrime(9));
	}

	@Test
	public void testAllUnder100Primes() {
		for (int i = 0; i < 100; i++) {
			if (basicPrimesSet.contains(i)) {
				assertTrue(i + " is a prime number", pnc.isPrime(i));
			} else {
				assertFalse(i + " is NOT a prime number", pnc.isPrime(i));
			}
		}
	}
}
