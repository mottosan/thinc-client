/*********************************************
 * Thinc Client
 * Amortya Ray & Divya Arora
 * {ar2566, da2254} @columbia.edu
 * Fall 2006-Fall 2007
 *********************************************/

package thinc;

import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.MemoryImageSource;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Collections;

/**
 * THINC Client Java Canvas. Extends AWT <code>{@link Canvas}</code> class.
 * All important rendering happens in this class.
 */

final class ThincClientCanvas extends Canvas {
	// static constants
	static final int[] DEFAULT_BAND_OFFSETS = new int[] { 2, 1, 0 };

	// member variables
	private int[] rawPixelIntArray;
	int[] rawPixels = null;
	int[] cursorPixels = null;
	int[] cursorMaskPixels = null;
	MemoryImageSource rawPixelsMis = null;
	MemoryImageSource cursorMaskMis = null;
	ByteBuffer cursorSourceBitMap = null;
	ByteBuffer cursorMaskBitMap = null;
	ColorModel canvasColorModel = null;
	ColorModel alphaColorModel = null;
	Image rawPixelImage = null;
	Image cursorImage = null;
	BufferedImage offScreenImage = null;
	Graphics2D offScreenGraphics = null;
	boolean IS_CURSOR_ARGB = true;

	// fb_header values
	short frameBufferWidth = -1;
	short frameBufferHeight = -1;
	byte frameBufferDepth = -1;
	byte frameBufferBpp = -1;
	int frameBufferSize = -1;

	// cursor values
	int cursorFg = -1;
	int cursorBg = -1;
	int cursorWidth = -1;
	int cursorHeight = -1;
	int cursorSize = -1;
	Point cursorHotSpot = null;

	/**
	 * Initializes the <code>ThincClientCanvas</code>.
	 */
	ThincClientCanvas(ThincProtocol proto) {
		// setting the focus traversal keys to the EMPTY_SET allows keyboard
		// to work.
		setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
				Collections.EMPTY_SET);
		setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
				Collections.EMPTY_SET);
		// setLayout(new BorderLayout());

		// add listeners
		addMouseListener(new ThincCMessageMouseButton(proto));
		addMouseMotionListener(new ThincCMessageMouseMotion(proto));
		addKeyListener(new ThincCMessageKeyEvent(proto));
	}

	// ----------------------- Utility methods -----------------------//
	/**
	 * This method creates a pixel array from a bitmap. It retrieves a byte to
	 * mask from the passed-in bitMap array and then reads each bit from the
	 * byte using a mask. If the mask has been used *width* times, then the next
	 * byte in the byte array is retrieved, regardless if the current byte has
	 * been fully masked.
	 * <p>
	 * The pixel array should be of size width*height of the image to be
	 * produced from the bitmap. If a "1" is found, the foreground int color is
	 * set to the current position of the array. Otherwise, the background int
	 * color is used.
	 * <p>
	 * In addition, this method is meant to update existing pixel arrays instead
	 * of generating new ones.
	 * 
	 * @param bitMap
	 *            array of <code>byte</code>s representing the bitmap
	 * @param fg
	 *            <code>int</code> representing foreground color to use
	 * @param bg
	 *            <code>int</code> representing background color to use
	 * @param width
	 *            <code>int</code> representing the width of the image to be
	 *            generated
	 */
	static void generateBitMapImage(byte[] bitMap, int[] imageArray, int fg,
			int bg, int width, int offset) {
		byte curByte, mask;
		// mask the pixels
		// :TODO: is there an easier way to make bitmaps????
		for (int i = 0, j = 0, maskCount = 0; i < offset; i++) {
			curByte = bitMap[i];
			mask = 1;
			while (mask != 0) {
				imageArray[j] = ((curByte & mask) == mask) ? fg : bg;
				// adjust mask by one
				mask <<= 1;
				// increment bitMapPixel counter
				j++;
				// increment maskCount
				maskCount++;
				if (maskCount == width) {
					// reset count once width is reached and move byte pointer
					maskCount = 0;
					break;
				}
			}
		}
	}

	/**
	 * argbToAbgr()
	 * 
	 * converts ARGB image to ABGR images.
	 * 
	 * @param src
	 *            source ARGB pixel
	 * @return ABGR image pixel
	 */
	public int argbToAbgr(int src) {
		int r, g, b, a;
		/* extract alpha, red, green, blue separately */
		a = (src & 0xff000000);
		r = (src & 0xff0000);
		g = (src & 0xff00);
		b = (src & 0xff);

		/* switch the position for red and blue */
		r >>= 16;
		b <<= 16;

		/* put the switched alpha, blue, green, red onto src */
		src = a;
		src |= r;
		src |= g;
		src |= b;

		return src;
	}

	/**
	 * Utility method that generates a cursor image based on a sourceBitMap and
	 * a maskBitMap.
	 * 
	 * :TODO: this code and the generateBitMapImage code could be merged somehow
	 */
	void updateCursor() {
		byte curSourceByte, curMaskByte, mask;

		cursorFg |= 0xff000000;
		cursorBg |= 0xff000000;

		// it is assumed that both the sourceBitMap and maskBitMap byte arrays
		// are the same size
		// System.out.println("curCAP:"+cursorSourceBitMap.capacity()+"
		// curLIM:"+cursorSourceBitMap.limit());
		// int bitMapLength = cursorSourceBitMap.capacity();
		// int bitMapLength = cursorSourceBitMap.limit();

		if (!IS_CURSOR_ARGB) {
			int bitMapLength = cursorSize / 2;
			// mask the pixels
			for (int i = 0, j = 0, maskCount = 0; i < bitMapLength; i++) {
				curSourceByte = cursorSourceBitMap.get(i);
				curMaskByte = cursorMaskBitMap.get(i);
				mask = 1;
				while (mask != 0) {
					if ((curMaskByte & mask) != mask) {
						cursorPixels[j] = 0;
					} else {
						cursorPixels[j] = ((curSourceByte & mask) == mask) ? cursorFg
								: cursorBg;
					}
					// adjust mask by one
					mask <<= 1;
					// increment bitMapPixel counter
					j++;
					// increment maskCount
					maskCount++;
					if (maskCount == cursorWidth) {
						// reset count once width is reached and move byte
						// pointer
						maskCount = 0;
						break;
					}
				}
			}
		} else {
			int bitMapLength = cursorSize / 8;
			for (int i = 0; i < bitMapLength; i++) {
				cursorPixels[i] = cursorSourceBitMap.getInt(i * 4);
			}
			for (int i = 0; i < bitMapLength; i++) {
				cursorPixels[i + bitMapLength] = cursorMaskBitMap.getInt(i * 4);
			}
		}

		cursorImage.flush();
		cursorMaskMis.newPixels();

		setCursor(getToolkit().createCustomCursor(cursorImage, cursorHotSpot,
				ThincProtocol.THINC_CURSOR));
		// logger.debug("Exiting updateCursor within ThincClientCanvas");
	}

	/**
	 * Debug method.
	 */
	String logSocketDebugInfo() {
		// logger.debug("Entering logSocketDebugInfo within ThincClientCanvas");
		StringBuffer sb = new StringBuffer();
		sb.append("Initial framebuffer parameters:\n");
		sb.append("Width: " + frameBufferWidth + "\n");
		sb.append("Height: " + frameBufferHeight + "\n");
		sb.append("Depth: " + frameBufferDepth + "\n");
		sb.append("Bits per pixel: " + frameBufferBpp + "\n");
		// logger.debug("Exiting logSocketDebugInfo within ThincClientCanvas");
		return sb.toString();
	}

	// ----------------------- Inherited AWT methods -----------------------//
	public void paint(Graphics g) {
		// logger.debug("Entering paint within ThincClientCanvas");
		g.drawImage(offScreenImage, 0, 0, null);
	}

	public void update(Graphics g) {
		// logger.debug("Entering update within ThincClientCanvas");
		paint(g);
		// /logger.debug("Exiting paint within ThincClientCanvas");
	}

	public void doRepaint(int x, int y, int w, int h) {
		// logger.debug("In doRepaint");
		repaint(30, x, y, w, h);
		// logger.debug("Done with doRepaint");
	}

	public boolean isOpaque() {
		return true;
	}

	/**
	 * Always returns true in order to properly handle key events.
	 * 
	 * @return true
	 */
	public boolean isFocusable() {
		return true;
	}

	public void drawImageScaled(IntBuffer bb, int x, int y, int w, int h,
			int dst_w, int dst_h, int type) throws ThincException {

		/* set the scaling factors onto AffineTransform object */
		AffineTransform trans = new AffineTransform();
		trans.setToScale(((double) dst_w / (double) w),
				((double) dst_h / (double) h));

		/* copy IntBuffer object from the server into rawPixel */
		rawPixelIntArray = new int[bb.limit()];
		bb.get(rawPixelIntArray);

		/*
		 * translate normal rgb into abgr. default BufferedImage object contains
		 * each pixel as ABGR, not RGB
		 */

		for (int i = 0; i < rawPixelIntArray.length; i++) {
			rawPixelIntArray[i] >>= 8;
			rawPixelIntArray[i] |= 0xff000000;
		}

		/* set various configuration for creating images */
		rawPixelsMis.setAnimated(true);
		rawPixelsMis.setFullBufferUpdates(false);

		/* create original image using createImage method */
		rawPixelImage = this.createImage(rawPixelsMis);

		BufferedImage videoImage = getGraphicsConfiguration()
				.createCompatibleImage(dst_w, dst_h);

		/*
		 * JFrame temp = new JFrame("tp"); Icon im = new ImageIcon(videoImage);
		 * JLabel xt = new JLabel(im); temp.add(xt); temp.setVisible(true);
		 */

		Graphics2D videoGraphics = videoImage.createGraphics();
		videoGraphics.setTransform(trans);
		videoGraphics.drawImage(rawPixelImage, 0, 0, null);
		videoGraphics.dispose();

		/* draw the scaled image onto canvas */

		this.offScreenGraphics.drawImage(videoImage, x, y, null);
		repaint(30, x, y, dst_w, dst_h);
		// paintImmediately(x, y, dst_w, dst_h);

	}

}
