/*********************************************
 * Thinc Client
 * Amortya Ray & Divya Arora
 * {ar2566, da2254} @columbia.edu
 * Fall 2006-Fall 2007
 *********************************************/

package thinc;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;

import com.sixlegs.png.PngImage;


//import com.sixlegs.png.PngImage;

/**
 * ThincSMessageRawUpdate. Handles raw updates sent by the server and paints
 * them on the <code>ThincClientCanvas</code>.
 */

final class ThincSMessageRawUpdate extends ThincSMessageBase {
	public static Logger logger = ThincLogger
			.getLogger(ThincSMessageRawUpdate.class);
	// static constants
	/** Value of this is "12" */
	static final byte MESSAGE_TYPE = 12;
	private static final int MESSAGE_LENGTH = 8; // T_FB_RUPDATE_SIZE

	// member variables
	private ByteBuffer cHeaderBuf;
	private ByteBuffer cBuf;
	private byte[] cArray;
	private byte[] updateArray;
	private ByteBuffer updateBuf;
	private short x;
	private short y;
	private short w;
	private short h;
	private int offset;
	private int zline;
	private int uline;
	private int bufSize;
	private int rectWidth;
	private int totalSize;

	ThincSMessageRawUpdate(ThincProtocol proto) {
		super(proto, MESSAGE_LENGTH);
		cHeaderBuf = proto
				.allocateHeaderByteBuffer(ThincProtocol.T_FB_ZDATA_SIZE);
		cArray = new byte[0];
		updateArray = new byte[0];
		cBuf = proto.allocateHeaderByteBuffer(16384);
		updateBuf = proto.wrapByteArray(updateArray);
		System.out.println("*********constructor called************");
	}

	/**
	 * Reads the initial framebuffer parameters.
	 * 
	 * @param sc
	 *            a <code>SocketChannel</code> from the select in <code>
	 * {@link ThincProtocol#startEventLoop}</code>
	 * @throws ThincException
	 *             if an error occurs while reading bytes from the server
	 */
	public void processServerMessage(SocketChannel sc) throws ThincException {
		logger
				.debug("Entering processServerMessage within ThincSMessageRawUpdate");
		ThincProtocol.debug("Got RawUpdate");

		readServerBytes(sc, bb);

		int bytesRead = 0;

		// always flip the buffer
		bb.flip();

		// get x, y, w, h
		x = bb.getShort();
		y = bb.getShort();
		w = bb.getShort();
		h = bb.getShort();

		logger.info("x:" + x + " y:" + y + " w:" + w + " h:" + h);
		// :HACK: KDE sometimes sends width values of 0. This is a hack to make
		// it work
		rectWidth = (w != 0) ? w * (canvas.frameBufferBpp / 8)
				: 1 * (canvas.frameBufferBpp / 8);
		totalSize = (h != 0) ? rectWidth * h : rectWidth;
		// bufSize = (w != 0) ? Math.abs(w) * Math.abs(h) *
		// (canvas.frameBufferBpp/8) : 1 * (canvas.frameBufferBpp/8);
		bufSize = totalSize;

		if (updateArray.length < bufSize || updateArray.length > bufSize) {
			updateArray = new byte[bufSize];
			updateBuf = proto.wrapByteArray(updateArray);
		}
		updateBuf.limit(bufSize);
		// System.out.println("rW:"+rectWidth+" rH:"+h+" bs:"+bufSize);

		Image im;

		ByteBuffer tempBuffer = proto.allocateHeaderByteBuffer(4);

		if (proto.isRawDataAddCache()) {
			System.out.println("Skipping add cache");
			// Stubbed for now
			readServerBytes(sc, tempBuffer);
			// bb.getInt();
		} else if (proto.isRawDataCache()) {
			System.out.println("Skipping cache");
			// Stubbed for now
			readServerBytes(sc, tempBuffer);
			// bb.getInt();
		}

		if (proto.isRawDataResized()) {
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
			// bb.getInt();
		}

		if (proto.isRawDataCompressed()) {
			// System.out.println("Skipping compressed");
			// // Stubbed for now
			// readServerBytes(sc, tempBuffer);
			// // bb.getInt();
			int count = 1;
			System.out.println("Was compressed");
			bytesRead = readCompressedData(sc, updateArray, 0);
			System.out.println("No of bytes read" + bytesRead);

			ByteArrayInputStream bin = new ByteArrayInputStream(cArray, 0,
					zline);
			try {
				BufferedImage bi = new PngImage().read(bin, true);
				int[] colors = new int[bi.getWidth() * bi.getHeight()];
				// int bih = bi.getHeight();
				// int biw = bi.getWidth();
				// logger.debug("Raw update height: "+bi.getHeight());
				// logger.debug("Raw update width: "+bi.getWidth());
				// logger.debug("Raw update pixel color:
				// "+bi.getRGB(x,y,w,h,colors,0,0));
				bi.getRGB(0, 0, bi.getWidth(), bi.getHeight(), colors, 0, bi
						.getWidth());
				for (int i = 0; i < colors.length; i++)
					colors[i] = canvas.argbToAbgr(colors[i]);
				bi.setRGB(0, 0, bi.getWidth(), bi.getHeight(), colors, 0, bi
						.getWidth());

				im = Toolkit.getDefaultToolkit().createImage(bi.getSource());
				canvas.offScreenGraphics.drawImage(im, x, y, w, h, null);
				canvas.doRepaint(x, y, w, h);
				// canvas.repaint();
				bb.clear();
				logger
						.debug("Exiting processServerMessage within ThincSMessageRawUpdate");
				return;

			} catch (IOException e) {
				e.printStackTrace();
			}

			// im = Toolkit.getDefaultToolkit().createImage(image);
		}

		else if (!proto.isRawDataCompressed()) {
			System.out.println("Was not compressed");
			bytesRead = readServerBytes(sc, updateBuf);
			updateBuf.flip();
			im = Toolkit.getDefaultToolkit().createImage(updateBuf.array(), 0,
					bufSize);

			IntBuffer ib = updateBuf.asIntBuffer();
			offset = x + (y * canvas.frameBufferWidth);

			for (int i = 0; i < h; i++) {
				ib.get(canvas.rawPixels, offset, w);
				offset += canvas.frameBufferWidth;
			}

			canvas.rawPixelsMis.newPixels(x, y, w, h, false);
			canvas.offScreenGraphics.setClip(x, y, w, h);
			canvas.offScreenGraphics.drawImage(canvas.rawPixelImage, x, y, x
					+ w, y + h, x, y, x + w, y + h, null);
			canvas.offScreenGraphics.setClip(0, 0, canvas.frameBufferWidth,
					canvas.frameBufferHeight);
			// canvas.doRepaint(x,y,w,h);
			// canvas.repaint();
			// always clear the buffer
			bb.clear();
			logger
					.debug("Exiting processServerMessage within ThincSMessageRawUpdate");
			return;

		}

	}

	/**
	 * This method is used to grab compressed data from the passed-in <code>
	 * SocketChannel</code>
	 * and decompresses the data into the passed-in <code>
	 * byte</code> array.
	 * 
	 * @param sc
	 *            a valid <code>SocketChannel</code> object
	 * @param byteArray
	 *            a <code>byte</code> array large enough to hold the
	 *            decompressed data
	 * @return number of bytes read
	 * @throws ThincException
	 *             if there is a problem with reading or decompressing data from
	 *             the <code>SocketChannel</code>
	 */
	protected int readCompressedData(SocketChannel sc, byte[] byteArray,
			int offset) throws ThincException {
		logger.debug("Entering readCompressed within ThincSMessageRawUpdate");
		System.out
				.println("Entering readCompressed within ThincSMessageRawUpdate");
		// :TODO: Need to centralize the reading of compressed data.
		readServerBytes(sc, cHeaderBuf);
		cHeaderBuf.flip();
		// read compressed scanline length
		zline = cHeaderBuf.getInt();
		// read uncompressed scanline length
		uline = cHeaderBuf.getInt();

		// System.out.println("zl:"+zline+" ul:"+uline);
		// :ALERT!: The resizing conditions here address cases where the
		// compressed data are actually *LARGER* than the uncompressed data.
		if ((cArray.length < uline || cArray.length > uline << 2)
				|| (cArray.length < zline)) {
			// cArray = new byte[((zline > uline) ? zline : uline) << 1];
			cArray = new byte[((zline > uline) ? zline : uline)];
		}

		// :ALERT!: Java 1.4.2 strictly enforces a heap limit for ByteBuffer
		// allocation. Because we call raw updates all of the time, we have to
		// be careful not to reallocate ByteBuffers to unreasonable sizes, hence
		// why we allocate only 16384 bytes to the cBuf object in the
		// constructor of this class. Without this loop, we run into strange
		// OutOfMemoryErrors. Note that these OutOfMemoryErrors do not occur in
		// versions of Java < 1.4.2.
		for (int zoff = 0, zlength = 0, zremaining = zline; zoff < zline; zoff += zlength, zremaining -= zlength) {
			if (zremaining < cBuf.capacity()) {
				cBuf.limit(zremaining);
			}
			zlength = readServerBytes(sc, cBuf);
			cBuf.flip();
			cBuf.get(cArray, zoff, zlength);
			// cBuf.get(byteArray,zoff,zlength);
			cBuf.clear();
			// System.out.println("limit:"+cBuf.limit()+"
			// pos:"+cBuf.position());
		}

		// ByteArrayInputStream bin = new ByteArrayInputStream(cArray, 0,
		// zline);
		// PngImage image = new PngImage(bin);
		//
		// Image im = Toolkit.getDefaultToolkit().createImage(image);
		cHeaderBuf.clear();
		// return im;

		// :BUG: for some reason, the amount of data *actually* decompressed
		// doesn't always equal the amount of data *expected* to be decompressed
		logger
				.debug("Exiting readCompressedData within ThincSMessageRawUpdate");
		return uline;
	}
}
// ByteArrayInputStream bin = new ByteArrayInputStream(cArray, 0, zline);
// PNGImageReader in = new PNGImageReader();
// in.setInput(bin);
// try {
// System.out.println("numimage:"+in.getNumImages(true));
// for (int i=0; i<in.getNumImages(true); ++i) {
// BufferedImage image = in.read(i);
// }
// } catch (IOException e) {
// throw new ThincException(e.toString(),e);
// }
//
// try {
// BufferedImage test = PNGDecoder.decode(bin);
// } catch (IOException e) {
// throw new ThincException(e.toString(),e);
// }
//    
// System.out.println("before decompress cA="+cArray.toString());
// System.out.println("before decompress cA="+cArray.length);
// proto.decompressor.setInput(cArray,0,zline);
//
// try {
// int ret = proto.decompressor.inflate(byteArray,offset,uline);
// System.out.println("after decompress: ret="+ret);
// } catch (DataFormatException e) {
// throw new ThincException(e.toString(),e);
// }
//
// proto.decompressor.reset();
