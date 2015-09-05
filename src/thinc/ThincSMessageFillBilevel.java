/*********************************************
 * Thinc Client
 * Amortya Ray & Divya Arora
 * {ar2566, da2254} @columbia.edu
 * Fall 2006-Fall 2007
 *********************************************/

package thinc;

import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;

/**
 * ThincSMessageFillBilevel. Fill the region described by (x,y) (width, height)
 * using the bitmap as a stipple to fill the region: If there's a 1 on the
 * bitmap the fg color specified in the message should be applied. If there's a
 * 0, the bg color specified in the message should be applied.
 * 
 */

final class ThincSMessageFillBilevel extends ThincSMessageBase {
	public static Logger logger = ThincLogger
			.getLogger(ThincSMessageFillBilevel.class);
	// static constants
	/** Value of this is "17" */
	static final byte MESSAGE_TYPE = 17;
	private static final int MESSAGE_LENGTH = 18;
	private static final int RECTREGION_LENGTH = 8;

	// member variables
	private ByteBuffer rectangleBuf;
	private ByteBuffer updateBuf;
	private byte[] updateArray;
	private short x;
	private short y;
	private short w;
	private short h;
	private int fg;
	private int bg;
	private short numRects;
	private short rectX;
	private short rectY;
	private short rectWidth;
	private short rectHeight;
	private Rectangle tileRect = new Rectangle();
	private BufferedImage tile;

	ThincSMessageFillBilevel(ThincProtocol proto) {
		super(proto, MESSAGE_LENGTH);
		rectangleBuf = proto.allocateHeaderByteBuffer(RECTREGION_LENGTH);
		updateArray = new byte[0];
		updateBuf = proto.wrapByteArray(updateArray);
	}

	/**
	 * Main processing function for this message. Reads bitmap data from the
	 * THINC server and paints the bitmap to the onscreen image.
	 * 
	 * @param sc
	 *            a <code>SocketChannel</code> from the select in <code>
	 * {@link ThincProtocol#startEventLoop}</code>
	 * @throws ThincException
	 *             if an error occurs while reading bytes from the server
	 */
	public void processServerMessage(SocketChannel sc) throws ThincException {
		logger
				.debug("Entering processServerMessage within ThincSMessageFillBilevel");
		ThincProtocol.debug("Got FillBilevel");

		int bytesRead = readServerBytes(sc, bb);

		// always flip the buffer
		bb.flip();

		x = bb.getShort();
		y = bb.getShort();
		w = bb.getShort();
		h = bb.getShort();
		fg = bb.getInt();
		bg = bb.getInt();
		// fg=canvas.argbToAbgr(fg);
		// bg=canvas.argbToAbgr(bg);
		numRects = bb.getShort();

		ByteBuffer tempBuffer = proto.allocateHeaderByteBuffer(4);

		if (proto.isFillGlyphAndBilevelAddCache()) {
			System.out.println("Skipping add cache");
			// Stubbed for now
			readServerBytes(sc, tempBuffer);
			// bb.getInt();
		} else if (proto.isFillGlyphAndBilevelCache()) {
			System.out.println("Skipping cache");
			// Stubbed for now
			readServerBytes(sc, tempBuffer);
			// bb.getInt();
		}
		if (proto.isFillGlyphAndBilevelResized()) {
			ByteBuffer tempBuffer1 = proto.allocateHeaderByteBuffer(8);

			System.out.println("Skipping resized");
			// Stubbed for now
			readServerBytes(sc, tempBuffer1);
			tempBuffer1.flip();
			// short width =
			System.out.println("flag resize width :" + tempBuffer1.getShort());
			System.out.println("flag resize height :" + tempBuffer1.getShort());
			System.out.println("flag resize size :" + tempBuffer1.getInt());
			// bb.getInt();
		}

		int bitMapSizeInBytes = (int) (Math.ceil((double) w / 8)) * h;
		int[] bitMapPixels = new int[w * h];

		if (updateArray.length < bitMapSizeInBytes
				|| updateArray.length > bitMapSizeInBytes << 2) {
			updateArray = new byte[bitMapSizeInBytes << 1];
			updateBuf = proto.wrapByteArray(updateArray);
		}
		updateBuf.limit(bitMapSizeInBytes);

		bytesRead = readServerBytes(sc, updateBuf);
		updateBuf.flip();

		ThincClientCanvas.generateBitMapImage(updateArray, bitMapPixels, fg,
				bg, w, bitMapSizeInBytes);

		if (null == tile || tile.getTileWidth() != w
				|| tile.getTileHeight() != h) {
			tile = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		} else {
			tile.flush();
		}

		tile.getRaster().setDataElements(0, 0, w, h, bitMapPixels);

		if (0 == numRects) {
			canvas.offScreenGraphics.drawImage(tile, x, y, w, h, null);
			canvas.doRepaint(x, y, w, h);
		} else {
			// okay, the bitMapImage is ready. get ready to tile it.
			tileRect.setBounds(x, y, w, h);
			canvas.offScreenGraphics.setPaint(new TexturePaint(tile, tileRect));

			for (short i = 0; i < numRects; i++) {
				bytesRead = readServerBytes(sc, rectangleBuf);
				rectangleBuf.flip();
				rectX = rectangleBuf.getShort();
				rectY = rectangleBuf.getShort();
				rectWidth = rectangleBuf.getShort();
				rectHeight = rectangleBuf.getShort();

				// draw image rectangles at given coordinates
				canvas.offScreenGraphics.fillRect(rectX, rectY, rectWidth,
						rectHeight);
				canvas.doRepaint(x, y, w, h);
				// canvas.repaint();
				rectangleBuf.clear();
			}
		}

		// always clear the buffer
		bb.clear();
		logger
				.debug("Exiting processServerMessage within ThincSMessageFillBilevel");
	}
}