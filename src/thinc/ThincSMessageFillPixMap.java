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
 * ThincSMessageFillPixMap. Fills an area given (x,y) coordinates and width and
 * height plus pixmap data.
 */

final class ThincSMessageFillPixMap extends ThincSMessageBase {
	public static Logger logger = ThincLogger
			.getLogger(ThincSMessageFillPixMap.class);
	// static constants
	/** Value of this is "15" */
	static final byte MESSAGE_TYPE = 15;
	private static final int MESSAGE_LENGTH = 10;
	private static final int RECTREGION_LENGTH = 8;

	// member variables
	private ByteBuffer rectangleBuf;
	private ByteBuffer updateBuf;
	private byte[] updateArray;
	private int[] pixels;
	private Rectangle tileRect;
	private short x;
	private short y;
	private short w;
	private short h;
	private short numRects;
	private short rectX;
	private short rectY;
	private short rectWidth;
	private short rectHeight;
	private BufferedImage tile;

	ThincSMessageFillPixMap(ThincProtocol proto) {
		super(proto, MESSAGE_LENGTH);
		rectangleBuf = proto.allocateHeaderByteBuffer(RECTREGION_LENGTH);
		updateArray = new byte[0];
		updateBuf = proto.wrapByteArray(updateArray);
		pixels = new int[0];
		tileRect = new Rectangle();
	}

	/**
	 * Main processing function for this message. Reads pixmap from the THINC
	 * server and paints rectangles on the onscreen image.
	 * 
	 * @param sc
	 *            a <code>SocketChannel</code> from the select in <code>
	 * {@link ThincProtocol#startEventLoop}</code>
	 * @throws ThincException
	 *             if an error occurs while reading bytes from the server
	 */
	public void processServerMessage(SocketChannel sc) throws ThincException {
		logger.debug("Entering processServerMessage within FillPixMap");
		ThincProtocol.debug("Got FillPixMap");
		// read bytes
		int bytesRead = readServerBytes(sc, bb);

		// always flip the buffer
		bb.flip();

		x = bb.getShort();
		y = bb.getShort();
		w = bb.getShort();
		h = bb.getShort();
		numRects = bb.getShort();

		ByteBuffer tempBuffer = proto.allocateHeaderByteBuffer(4);

		if (proto.isPixMapAddCache()) {
			System.out.println("Skipping add cache");
			// Stubbed for now
			readServerBytes(sc, tempBuffer);
			// bb.getInt();
		} else if (proto.isPixMapCache()) {
			System.out.println("Skipping cache");
			// Stubbed for now
			readServerBytes(sc, tempBuffer);
			// bb.getInt();
		}
		if (proto.isPixMapResized()) {
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

		int newPixMapSizeInBytes = (w * canvas.frameBufferBpp / 8) * h;
		int newPixMapSizeInInts = newPixMapSizeInBytes / 4;

		if (updateArray.length < newPixMapSizeInBytes
				|| updateArray.length > newPixMapSizeInBytes << 2) {
			updateArray = new byte[newPixMapSizeInBytes << 1];
			pixels = new int[newPixMapSizeInInts << 1];
			updateBuf = proto.wrapByteArray(updateArray);
		}
		updateBuf.limit(newPixMapSizeInBytes);

		bytesRead = readServerBytes(sc, updateBuf);

		updateBuf.flip();
		updateBuf.asIntBuffer().get(pixels, 0, newPixMapSizeInInts);

		if (null == tile || tile.getTileWidth() != w
				|| tile.getTileHeight() != h) {
			tile = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		}

		tile.getRaster().setDataElements(0, 0, w, h, pixels);

		tileRect.setBounds(x, y, w, h);
		canvas.offScreenGraphics.setPaint(new TexturePaint(tile, tileRect));

		for (short i = 0; i < numRects; i++) {
			bytesRead = readServerBytes(sc, rectangleBuf);
			rectangleBuf.flip();
			rectX = rectangleBuf.getShort();
			rectY = rectangleBuf.getShort();
			rectWidth = rectangleBuf.getShort();
			rectHeight = rectangleBuf.getShort();

			canvas.offScreenGraphics.fillRect(rectX, rectY, rectWidth,
					rectHeight);
			canvas.doRepaint(rectX, rectY, rectWidth, rectHeight);
			// canvas.repaint();
			rectangleBuf.clear();
		}

		// always clear the buffer
		bb.clear();
		logger.debug("Exiting processServerMessage within FillPixMap");
	}
}
