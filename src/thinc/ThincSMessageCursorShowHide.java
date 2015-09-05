/*********************************************
 * Thinc Client
 * Amortya Ray & Divya Arora
 * {ar2566, da2254} @columbia.edu
 * Fall 2006-Fall 2007
 *********************************************/

package thinc;

import java.nio.channels.SocketChannel;
import org.apache.log4j.Logger;

/**
 * ThincSMessageCursorShowHide. Currently just a placeholder. Does nothing for
 * now.
 */

final class ThincSMessageCursorShowHide extends ThincSMessageBase {
	public static Logger logger = ThincLogger
			.getLogger(ThincSMessageCursorShowHide.class);
	// static constants
	/** Value of this is "41" */
	static final byte MESSAGE_TYPE = 41;
	private static final int MESSAGE_LENGTH = 1;

	ThincSMessageCursorShowHide(ThincProtocol proto) {
		super(proto, MESSAGE_LENGTH);
	}

	/**
	 * Main processing function for this message. Reads bitmap data from the
	 * THINC server and changes the cursor image.
	 * 
	 * @param sc
	 *            a <code>SocketChannel</code> from the select in <code>
	 * {@link ThincProtocol#startEventLoop}</code>
	 * @throws ThincException
	 *             if an error occurs while reading bytes from the server
	 */
	public void processServerMessage(SocketChannel sc) throws ThincException {
		logger
				.debug("Entering processServerMessage within ThincsMessageCursorShowHide");
		// read bytes
		int bytesRead = readServerBytes(sc, bb);

		// always flip the buffer
		bb.flip();

		byte showHide = bb.get();

		// always clear the buffer
		bb.clear();
		logger
				.debug("Exiting processServerMessage within ThincsMessageCursorShowHide");
	}
}
