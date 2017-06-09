# Experimental Implementation of Distributed Prime Number Check (DPNC) using NIO (non-blocking IO)

## Requirements

You are to create two small applications for this programming task; one is called Randomizer, the other Prime Randomizer‘s job is to generate a series of positive random integers and send those to Prime via a distributed queue of integers.

Primes job is to receive the integers and calculate whether the integer is a prime or not and return the answer to Randomizer via a distributed queue that contains the original number and a Boolean; which Randomizer will print to system out.

Points

   1. Use only the standard java library
   2. Both Applications will run on the same server
   3. The system should be as fast as possible
   4. The results do not have to be returned in the same order as received
   
## Implementation notes - new NIO server

This implementation should be considered experimental, still under construction, but in "works for me" state. 

This version uses only one thread for the whole socket server side, using Server Socket Channels and client Socket Channels, *both* in non-blocking mode.
No more new thread per client, which can easily exhaust available processing resources, while most threads could be just idle in waiting states.  

This solution is better suited for potentially very high number of clients, soon threads and connections resources would run out. 

For client-server communication stateful TCP is still used so even this NIO variant is prone no connection exhaustion with very high number of clients. 
To mitigate this, a new UDP based stateless connections protocol would have to be developed. 
 
Queue interface was limited to just to basic (blocking) operations: `put()` and `take()`.
 
I tried to keep the structure as similar as possible to the original solution.

Notes:

 * I had to change client-server protocol; clients now has to return data frame size (because response size can vary depending on size of the integer in response!)  
   I was easy to get around this using blocking IO, DataInputStream simply blocker reading call until rest of the data arrived.
   Now, response has to be fully buffered before moving it forward for decoding. 
   
 * Client is still implemented using standard blocking IO. Client is one-threaded application by nature, blocking IO possess no issues here.
 
 * Interface `ClientProxyProcess` was split to separate read() and write() operations. This time, with NIO, I could not postpone it anymore. 
 

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
