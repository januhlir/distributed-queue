package org.github.januhlir.dpnc.prime;

/**
 * Simple implementation of Prime Number check.
 * 
 * It can be improved slightly by testing on even number by using bit mask and
 * excluding them straight away, and similar hacks, but I assume that hyper
 * optimal check was not point of this assignment.
 * 
 * @author Jan.Uhlir
 */
public class PrimeNumberCheckSimple implements PrimeNumberCheck {

	@Override
	public boolean isPrime(int n) {
		if (n <= 1) return false; // 0, 1, negative numbers are not prime numbers by definition
		
		for (int i = 2; i <= Math.sqrt(n); i++) {
			if (n % i == 0) return false;
		}
		return true;
	}
}
