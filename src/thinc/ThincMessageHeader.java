/*********************************************
 * Thinc Client
 * Amortya Ray & Divya Arora
 * {ar2566, da2254} @columbia.edu
 * Fall 2006-Fall 2007
 *********************************************/

package thinc;

/**
 * ThincMessageHeader. Simple class to encapsulate message header data. All
 * messages from client to server and server to client use this structure. The
 * structure contains 4 bytes: 1 byte for the message type, 1 byte for any
 * message flags, and 2 bytes representing the length of the message.
 */

import java.nio.ByteBuffer;
import org.apache.log4j.Logger;

final class ThincMessageHeader {

	static final byte MESSAGE_LENGTH = 4;

	// Logger logger=ThincLogger.getLogger(ThincMessageHeader.class);
	public void setHeader(ByteBuffer bb, byte type, byte flags, short length) {
		// logger.debug("Entering and exiting setHeader");
	}

	byte type;
	byte flags;
	short length;

}