/*********************************************
 * Thinc Client
 * Amortya Ray & Divya Arora
 * {ar2566, da2254} @columbia.edu
 * Fall 2006-Fall 2007
 *********************************************/

package thinc;

import java.nio.channels.SocketChannel;
import java.nio.ByteBuffer;

import java.awt.image.MemoryImageSource;
import org.apache.log4j.Logger;

/**
 * ThincSMessageCursorChange. Handles changing the "hotspot" of the cursor.
 */

final class ThincSMessageCursorChange extends ThincSMessageBase {
	public static Logger logger = ThincLogger
			.getLogger(ThincSMessageCursorChange.class);
	// static constants
	/** Value of this is "40" */
	static final byte MESSAGE_TYPE = 40;
	private static final int MESSAGE_LENGTH = 4; // T_CURSOR_CHANGE_SIZE
	private byte[] updateArray;
	private ByteBuffer updateBuf;
	private int bufSize;

	ThincSMessageCursorChange(ThincProtocol proto) {
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
				.debug("Entering processServerMessage within ThinSMessageCursorChange");
		// read bytes
		int bytesRead = readServerBytes(sc, bb);

		// always flip the buffer
		bb.flip();

		// first buffer byte is the cursorXHotSpot. second buffer byte is the
		// cursorYHotSpot
		byte xhot = bb.get();
		byte yhot = bb.get();
		short size = bb.getShort();

		canvas.cursorHotSpot.move(xhot, yhot);
		canvas.cursorSize = size;
		// canvas.cursorHotSpot.move(bb.get(),bb.get());

		if ((proto.sHeader.flags & proto.T_CURSOR_ARGB) > 0)
			canvas.IS_CURSOR_ARGB = true;
		else
			canvas.IS_CURSOR_ARGB = false;
		// System.out.println("CURsize:"+size+"
		// CURcap:"+canvas.cursorSourceBitMap.capacity());
		// System.out.println("CURlmt:"+canvas.cursorSourceBitMap.limit()+"
		// CURcap:"+canvas.cursorSourceBitMap.capacity());
		// System.out.println("CURflags:"+proto.sHeader.flags+"
		// IS_CURSOR_ARGB"+canvas.IS_CURSOR_ARGB);

		// now update the cursor
		// first, the source bitmap
		canvas.cursorSourceBitMap.limit(size / 2);
		bytesRead = readServerBytes(sc, canvas.cursorSourceBitMap);
		canvas.cursorSourceBitMap.flip();

		// next, the mask bitmap
		canvas.cursorMaskBitMap.limit(size / 2);
		bytesRead = readServerBytes(sc, canvas.cursorMaskBitMap);
		canvas.cursorMaskBitMap.flip();

		// update cursor
		canvas.updateCursor();
		// canvas.repaint();

		// always clear the buffer
		canvas.cursorSourceBitMap.clear();
		canvas.cursorMaskBitMap.clear();
		bb.clear();
		logger
				.debug("Exiting processServerMessage within ThinSMessageCursorChange");
	}
}
