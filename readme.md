Requirements

You are to create two small applications for this programming task; one is called Randomizer, the other Prime Randomizer‘s job is to generate a series of positive random integers and send those to Prime via a distributed queue of integers.

Primes job is to receive the integers and calculate whether the integer is a prime or not and return the answer to Randomizer via a distributed queue that contains the original number and a Boolean; which Randomizer will print to system out.

Points

   1. Use only the standard java library
   2. Both Applications will run on the same server
   3. The system should be as fast as possible
   4. The results do not have to be returned in the same order as received

## Implementation - Distributed Prime Number Check (DPNC)

Simple implementation of Distributed Processing Queue based on socket server and one thread per
client proxy, and the remote clients itself. This solution supports technically unlimited number of participating 
remote clients; they can be added or dropped any time.

This solution is not well suited for very high number of clients, soon threads and connections resources would run out. To improve
this, NIO would have to be used, to lower number of needed threads; and UDP stateless connections.
 
Queue interface was limited to just to basic (blocking) operations: `put()` and `take()`.
 
Implementation by me, Jan Uhlir.

### Quick Start

Run `RunTestSetup` class. It starts Randomizer, the server, and 5 Prime clients; all as separate applications communicating via sockets. 

The Randomizer prints results:

	Number 50 is NOT prime number
	Number 79 is a prime number
	Number 63 is NOT prime number
	Number 4 is NOT prime number
	Number 49 is NOT prime number
	Number 43 is a prime number
	Number 9 is NOT prime number
	Number 66 is NOT prime number
	Number 3 is a prime number
	Number 84 is NOT prime number
	Number 7 is a prime number
	Number 50 is NOT prime number
	Number 27 is NOT prime number
	Number 20 is NOT prime number

### Manual Start

Run `RandomizerApplicationRun`, the server, first. Then add clients by running `PrimeApplicationRun`. The Randomizer prints results, see example above. To see any results
at least one connected client has to be running.

Note: the server port is hard coded to 15678.

### Parts

Packages:

 * prime - Prime number check client implementation 
 * randomizer - random integer generating, and result printing, server implementation.
 * queue - generic distributed processing queue implementation, see `DistributedProcessQueue` class. 
