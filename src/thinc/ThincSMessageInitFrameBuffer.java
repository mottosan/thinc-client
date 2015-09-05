/*********************************************
 * Thinc Client
 * Amortya Ray & Divya Arora
 * {ar2566, da2254} @columbia.edu
 * Fall 2006-Fall 2007
 *********************************************/

package thinc;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DirectColorModel;
import java.awt.image.MemoryImageSource;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.zip.DataFormatException;

import org.apache.log4j.Logger;

/**
 * ThincSMessageInitFrameBuffer. Handles the initial framebuffer dump. Sets the
 * <code>ColorModel</code>, the <code>Frame</code> width and height, the
 * image pixels, and creates an <code>Image</code> to be painted on the <code>
 * ThincClientCanvas</code>.
 */

final class ThincSMessageInitFrameBuffer extends ThincSMessageBase {
	public static Logger logger = ThincLogger
			.getLogger(ThincSMessageInitFrameBuffer.class);
	// static constants
	/** Value of this is "11" */
	static final byte MESSAGE_TYPE = 11;
	private static final int MESSAGE_LENGTH = 24;
	public static boolean appletflag;

	ThincSMessageInitFrameBuffer(ThincProtocol proto, boolean app) {
		super(proto, MESSAGE_LENGTH);
		appletflag = app;
	}

	/**
	 * Reads the initial framebuffer parameters and sets up
	 * <code>ThincClientCanvas</code> for double buffering.
	 * 
	 * @param sc
	 *            a <code>SocketChannel</code> from the select in <code>
	 * {@link ThincProtocol#startEventLoop}</code>
	 * @throws ThincException
	 *             if an error occurs while reading bytes from the server
	 */

	public void processServerMessage(SocketChannel sc) throws ThincException {
		logger
				.debug("Entering processServerMessage within ThincSMessageInitFrameBuffer");
		// init colorModels
		initColorModels();

		// init cursor
		initCursor(sc);
		// // VERY IMPORTANT METHOD: initializes a lot of member variables
		// initCanvasValues(sc);

		if (proto.isRawDataCompressed()) {
			System.out.println("Data is Compressed.");
			readCompressedData(sc, canvas.rawPixels);
		} else {
			readPixels(sc, canvas.rawPixels, 0);
		}
		// :DEBUG: print out the framebuffer initial values
		System.out.println(canvas.logSocketDebugInfo());

		canvas.rawPixelsMis = new MemoryImageSource(canvas.frameBufferWidth,
				canvas.frameBufferHeight, canvas.canvasColorModel,
				canvas.rawPixels, 0, canvas.frameBufferWidth);
		canvas.rawPixelsMis.setAnimated(true);
		canvas.rawPixelsMis.setFullBufferUpdates(false);

		canvas.rawPixelImage = canvas.createImage(canvas.rawPixelsMis);

		canvas.offScreenImage = canvas.getGraphicsConfiguration()
				.createCompatibleImage(canvas.frameBufferWidth,
						canvas.frameBufferHeight);
		canvas.offScreenGraphics = (Graphics2D) canvas.offScreenImage
				.getGraphics();
		canvas.offScreenGraphics.drawImage(canvas.rawPixelImage, 0, 0, null);

		// send good ack. this is as of version 0.1.1
		// proto.sendGoodAck();

		canvas.setSize(canvas.frameBufferWidth, canvas.frameBufferHeight);
		if (appletflag == false) {
			// set frame to visible before creating other images
			proto.frame.pack();
			proto.frame.setMaximizedBounds(proto.frame.getBounds());
			// :TODO: make resizable, but only to the maximum
			// frameBufferWidth/Height

			proto.frame.setVisible(true);
			// proto.frame.setResizable(true);
		} else {
			proto.main.setVisible(true);
		}

		// canvas.repaint();

		bb.clear();
		logger
				.debug("Exiting processServerMessage within ThincSMessageInitFrameBuffer");
	}

	/**
	 * This method initializes the color models for the offscreen
	 * <code>{@link BufferedImage}</code> and the cursor image. :TODO: need to
	 * handle color models more appropriately for different depths and such
	 */
	private void initColorModels() {
		logger
				.debug("Entering initColorModels within ThincSMessageInitFrameBuffer");
		canvas.canvasColorModel = new DirectColorModel(24,
		// 0xff0000,0x00ff00,0x0000ff);
				// 0xff0000, 0xff0000, 0xff0000); //GRAY
				// 0xff0000, 0xff0000, 0x00ff00);//BEST GRAY(R), YELLOW, BLUE
				// 0xff0000, 0xff0000, 0x0000ff); //YELLOW
				0x00ff00, 0xff0000, 0xff0000); // GRAY(R), BLUE/GREEN, RED
		// 0x00ff00, 0xff0000, 0x00ff00); //GRAY(G), GREEN, PURPLE
		// 0x00ff00, 0xff0000, 0x0000ff); //YELLOW, GREEN, RED
		// 0x0000ff, 0xff0000, 0xff0000); TEAL
		// 0x0000ff, 0xff0000, 0x00ff00); //TEAL, YELLOW, BLUE
		// 0x0000ff, 0xff0000, 0x0000ff); GREEN

		// 0xff0000, 0x00ff00, 0xff0000); GRAY(PURPLE), PURPLE, GREEN
		// 0xff0000, 0x00ff00, 0x00ff00); GRY(RED), RED, LIGHT BLUE
		// 0xff0000, 0x00ff00, 0x0000ff); YELLOW, RED, LIGHT GREEN
		// 0x00ff00, 0x00ff00, 0xff0000); GRAY(B), BLUE, YELLOW
		// 0x00ff00, 0x00ff00, 0x00ff00); GRAY
		// 0x00ff00, 0x00ff00, 0x0000ff); YELLOW
		// 0x0000ff, 0x00ff00, 0xff0000); TEAL (YELLOW, GREEN)
		// 0x0000ff, 0x00ff00, 0x00ff00); TEALISH
		// 0x0000ff, 0x00ff00, 0x0000ff); GREEN

		// 0xff0000, 0x0000ff, 0xff0000); PURPLE
		// 0xff0000, 0x0000ff, 0x00ff00); PURPLE, RED, BLUE
		// 0xff0000, 0x0000ff, 0x0000ff); RED
		// 0x00ff00, 0x0000ff, 0xff0000); //PURPLE, BLUE, RED
		// 0x00ff00, 0x0000ff, 0x00ff00); PURPLE
		// 0x00ff00, 0x0000ff, 0x0000ff); //RED
		// 0x0000ff, 0x0000ff, 0xff0000); BLUE
		// 0x0000ff, 0x0000ff, 0x00ff00); BLUE
		// 0x0000ff, 0x0000ff, 0x0000ff); BLACK
		// canvas.canvasColorModel = new
		// ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_CIEXYZ)
		// , true, true, Transparency.TRANSLUCENT, DataBuffer.TYPE_USHORT);

		// :HACK: this alpha color model is necessary to draw the bitmap on the
		// screen. it provides an alpha channel at 32 bits, although we may not
		// necessarily be using 32 bits at all times in the future.

		canvas.alphaColorModel = new DirectColorModel(32, 0x00ff0000,
				0x0000ff00, 0x000000ff, 0xff000000);
		// canvas.alphaColorModel = new DirectColorModel(32,0x00ff00, 0xff0000,
		// 0xff0000);
		logger
				.debug("Exiting initColorModels within ThincSMessageInitFrameBuffer");
	}

	/**
	 * Creates the cursor image. Must be called after {@link #initCanvasValues}.
	 * 
	 * @param sc
	 * @throws ThincException
	 */
	private void initCursor(SocketChannel sc) throws ThincException {
		// cursor information. if the cursor size is a negative value, then
		// cursor is not drawn locally.
		logger.debug("Entering initCursor within ThincSMessageInitFrameBuffer");
		if (canvas.cursorSize <= 0) {
			return;
		}

		// initialize cursor byte arrays and buffers
		canvas.cursorPixels = new int[canvas.cursorWidth * canvas.cursorHeight
				* 4];

		// now do the cursor mask
		canvas.cursorMaskMis = new MemoryImageSource(canvas.cursorWidth,
				canvas.cursorHeight, canvas.alphaColorModel,
				canvas.cursorPixels, 0, canvas.cursorWidth);
		canvas.cursorMaskMis.setAnimated(false);
		canvas.cursorMaskMis.setFullBufferUpdates(false);

		canvas.cursorImage = canvas.createImage(canvas.cursorMaskMis);

		// update the cursor
		canvas.updateCursor();

		// always clear the buffers
		canvas.cursorSourceBitMap.clear();
		canvas.cursorMaskBitMap.clear();
		logger.debug("Exiting initCursor within ThincSMessageInitFrameBuffer");
	}

	/**
	 * This method is used to grab compressed data from the passed-in <code>
	 * SocketChannel</code>
	 * and decompresses the data into the passed-in <code>
	 * byte</code> array.
	 * 
	 * @param sc
	 *            a valid <code>SocketChannel</code> object
	 * @param pixels
	 *            a <code>byte</code> array large enough to hold the
	 *            decompressed data
	 * @return number bytes read
	 * @throws ThincException
	 *             if there is a problem with reading or decompressing data from
	 *             the <code>SocketChannel</code>
	 */
	private int readCompressedData(SocketChannel sc, byte[] pixels)
			throws ThincException {
		logger
				.debug("Entering readCompressedData within ThincSMessageInitFrameBuffer");
		// :TODO: Need to centralize the reading of compressed data.
		int zline, uline;
		ByteBuffer readLineBuf = null;
		ByteBuffer cHeaderBuf = proto.allocateHeaderByteBuffer(8);
		byte[] readLineByteArray = null;
		int offset = 0;
		try {
			for (int i = 0; i < canvas.frameBufferHeight; i++) {
				readServerBytes(sc, cHeaderBuf);
				cHeaderBuf.flip();
				// read compressed scanline length
				zline = cHeaderBuf.getInt();
				// read uncompressed scanline length
				uline = cHeaderBuf.getInt();

				ThincProtocol.debug("zline: " + zline);
				ThincProtocol.debug("uline: " + uline);
				if (null == readLineByteArray
						|| readLineByteArray.length < zline) {
					ThincProtocol.debug("Reallocating readLineBuf.");
					// if(zline>10000) zline = zline >> 1;
					// System.out.println(cHeaderBuf.get(0)+"
					// "+cHeaderBuf.get(1)+" "+cHeaderBuf.get(2)+"
					// "+cHeaderBuf.get(3));
					// readLineByteArray = new byte[zline * 2];
					readLineByteArray = new byte[zline * 2];
					readLineBuf = proto.wrapByteArray(readLineByteArray, 0,
							zline);
				} else if (readLineByteArray.length > zline) {
					readLineBuf = proto.wrapByteArray(readLineByteArray, 0,
							zline);
				}

				ThincProtocol.debug("Reading "
						+ readServerBytes(sc, readLineBuf)
						+ " bytes of compressed data.");
				readLineBuf.flip();

				// now decompress the data
				proto.decompressor.setInput(readLineBuf.array(), 0, zline);
				offset += proto.decompressor.inflate(pixels, offset, uline);
				ThincProtocol.debug("Decompressed " + offset + " bytes.");
				proto.decompressor.reset();
				cHeaderBuf.clear();
			}
		} catch (DataFormatException e) {
			throw new ThincException(e.toString(), e);
		}
		logger
				.debug("Exiting readCompressedData within ThincSMessageInitFrameBuffer");
		return offset;
	}

	/**
	 * Method to read compressed data into an <code>int</code> array.
	 * 
	 * @param sc
	 *            a valid <code>SocketChannel</code> object
	 * @param pixels
	 *            an <code>int</code> array large enough to hold the
	 *            decompressed data
	 * @return number bytes read
	 * @throws ThincException
	 */
	private int readCompressedData(SocketChannel sc, int[] pixels)
			throws ThincException {
		logger
				.debug("Entering readCompressedData within ThincSMessageInitFrameBuffer");
		byte[] bufArray = new byte[canvas.frameBufferSize];
		int bytesRead = readCompressedData(sc, bufArray);
		proto.wrapByteArray(bufArray).asIntBuffer().get(pixels);
		logger
				.debug("Exiting readCompressedData within ThincSMessageInitFrameBuffer");
		return bytesRead;
	}
}
