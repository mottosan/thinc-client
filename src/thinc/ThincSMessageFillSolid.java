/*********************************************
 * Thinc Client
 * Amortya Ray & Divya Arora
 * {ar2566, da2254} @columbia.edu
 * Fall 2006-Fall 2007
 *********************************************/

package thinc;

import java.awt.Color;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;

/**
 * ThincSMessageFillSolid. Tells the client to fill the area described by
 * x,y,w,h with a pixel value.
 */

final class ThincSMessageFillSolid extends ThincSMessageBase {
	public static Logger logger = ThincLogger
			.getLogger(ThincSMessageFillSolid.class);
	// static constants
	/** Value of this is "14" */
	static final byte MESSAGE_TYPE = 14;
	private static final int MESSAGE_LENGTH = 6;
	private static final int RECTREGION_LENGTH = 8;

	// member variables
	private ByteBuffer rectangleBuf = null;
	private int pixel = -1;
	private short numRects = -1;
	private short rectX = -1;
	private short rectY = -1;
	private short rectWidth = -1;
	private short rectHeight = -1;

	ThincSMessageFillSolid(ThincProtocol proto) {
		super(proto, MESSAGE_LENGTH);
		rectangleBuf = proto.allocateHeaderByteBuffer(RECTREGION_LENGTH);
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
		logger
				.debug("Entering processServerMessage within ThincSMessageFillSolid");
		ThincProtocol.debug("Got FillSolid");

		int bytesRead = readServerBytes(sc, bb);
		// always flip the buffer
		bb.flip();
		// bb.getShort();
		pixel = bb.getInt();
		// pixel=canvas.argbToAbgr(pixel);
		numRects = bb.getShort();
		canvas.offScreenGraphics.setColor(new Color(pixel));

		for (short i = 0; i < numRects; i++) {
			bytesRead = readServerBytes(sc, rectangleBuf);
			rectangleBuf.flip();
			// bb.getShort();
			rectX = rectangleBuf.getShort();
			rectY = rectangleBuf.getShort();
			rectWidth = rectangleBuf.getShort();
			rectHeight = rectangleBuf.getShort();

			ThincProtocol.debug("x:" + rectX + " y:" + rectY + " w:"
					+ rectWidth + " h:" + rectHeight);
			canvas.offScreenGraphics.fillRect(rectX, rectY, rectWidth,
					rectHeight);
			canvas.doRepaint(rectX, rectY, rectWidth, rectHeight);

			// canvas.repaint();
			// proto.frame.setVisible(false);
			// proto.frame.setVisible(true);
			rectangleBuf.clear();
		}

		// always clear the buffer
		bb.clear();
		logger
				.debug("Exiting processServerMessage within ThincSMessageFillSolid");
		// proto.sendUpdaterequest();
	}
}

// }
