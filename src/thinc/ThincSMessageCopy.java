/*********************************************
 * Thinc Client
 * Amortya Ray & Divya Arora
 * {ar2566, da2254} @columbia.edu
 * Fall 2006-Fall 2007
 *********************************************/

package thinc;

import java.awt.BorderLayout;
import java.awt.Button;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;
import org.apache.log4j.Logger;

/**
 * ThincSMessageCopy. Handles CopyRect messages from the THINC server and paints
 * updates on to the <code>ThincClientCanvas</code>.
 */

final class ThincSMessageCopy extends ThincSMessageBase {
	public static Logger logger = ThincLogger
			.getLogger(ThincSMessageCopy.class);
	// static constants
	/** Value of this is "13" */
	static final byte MESSAGE_TYPE = 13;
	private static final int MESSAGE_LENGTH = 12;

	// member variables
	private short sx;
	private short sy;
	private short dx;
	private short dy;
	private short width;
	private short height;

	ThincSMessageCopy(ThincProtocol proto) {
		super(proto, MESSAGE_LENGTH);
	}

	/**
	 * Tells the client to copy the area of size width*height, with top left
	 * corner (srcx,srcy) to (dstx,dsty).
	 * 
	 * @param sc
	 *            a <code>SocketChannel</code> from the select in <code>
	 * {@link ThincProtocol#startEventLoop}</code>
	 * @throws ThincException
	 *             if an error occurs while reading bytes from the server
	 */
	public void processServerMessage(SocketChannel sc) throws ThincException {
		logger.debug("Entering processServerMessage within ThincSMessageCopy");
		ThincProtocol.debug("Got Copy");
		// read bytes
		System.out.println(proto.sHeader.length);
		int bytesRead = readServerBytes(sc, bb);

		// always flip the buffer
		bb.flip();

		sx = bb.getShort();
		sy = bb.getShort();
		dx = bb.getShort();
		dy = bb.getShort();
		width = bb.getShort();
		height = bb.getShort();

		// get canvas and call copyArea on buffer
		canvas.offScreenGraphics.copyArea(sx, sy, width, height, dx - sx, dy
				- sy);

		canvas.doRepaint(dx, dy, width, height);
		// canvas.repaint();

		// always clear the buffer
		bb.clear();
		logger.debug("Exiting processServerMessage within ThincSMessageCopy");
	}
}
