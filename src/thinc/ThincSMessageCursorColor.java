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
 * ThincSMessageCursorColor. Changes the background and foreground color of the
 * cursor.
 */

final class ThincSMessageCursorColor extends ThincSMessageBase {

	public static Logger logger = ThincLogger
			.getLogger(ThincSMessageCursorColor.class);
	// static constants
	/** Value of this is "43" */
	static final byte MESSAGE_TYPE = 43;
	private static final int MESSAGE_LENGTH = 8;

	ThincSMessageCursorColor(ThincProtocol proto) {
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
				.debug("Entering processServerMessage within ThincSMessageCursorColor");
		// read bytes
		int bytesRead = readServerBytes(sc, bb);

		// always flip the buffer
		bb.flip();

		canvas.cursorFg = bb.getInt();
		canvas.cursorBg = bb.getInt();

		// update the cursor pixel array
		// System.out.println("curCAP:"+canvas.cursorSourceBitMap.capacity()+"
		// curLIM:"+canvas.cursorSourceBitMap.limit());
		canvas.updateCursor();
		// canvas.repaint();
		// always clear the buffer
		bb.clear();
		logger
				.debug("Exiting processServerMessage within ThincSMessageCursorColor");
	}
}
