/*********************************************
 * Thinc Client
 * Amortya Ray & Divya Arora
 * {ar2566, da2254} @columbia.edu
 * Fall 2006-Fall 2007
 *********************************************/

package thinc;

import java.nio.channels.SocketChannel;

/**
 * ThincSMessage. Implemented by <code>ThincSMessageBase</code>. It is
 * rumored that invoking virtual methods from an interface is faster than from
 * an abstract base class, but I'm not so sure. Still, I'm being superstitious
 * and implementing this interface.
 * 
 */

interface ThincSMessage {
	abstract void processServerMessage(SocketChannel sc) throws ThincException;
}
