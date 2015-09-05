/*********************************************
 * Thinc Client
 * Amortya Ray & Divya Arora
 * {ar2566, da2254} @columbia.edu
 * Fall 2006-Fall 2007
 *********************************************/

package thinc;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * ThincSMessageBase base class. All classes that represent server-client
 * messages must extend this class.
 */

abstract class ThincSMessageBase implements ThincSMessage {

	// member variables
	protected ThincProtocol proto;
	protected ThincClientCanvas canvas;
	protected ByteBuffer bb;

	// private member variables
	private ByteBuffer buf;

	protected ThincSMessageBase() {
		// no instantiation without args
	}

	/**
	 * Default constructor for all <code>ThincSMessageBase</code>s.
	 * 
	 * @param messageLength
	 *            an int representing the length of the message's
	 *            <code>ByteBuffer</code>
	 */
	protected ThincSMessageBase(ThincProtocol proto, int messageLength) {
		this.proto = proto;
		this.bb = proto.allocateHeaderByteBuffer(messageLength);
		// for convenience, keeping handle to canvas locally
		this.canvas = proto.canvas;

		// for readPixelsArray(SocketChannel, int[])
		this.buf = proto.allocateHeaderByteBuffer(4);
	}

	/**
	 * Handles reading data from the THINC server that is the size of the given
	 * ByteBuffer.
	 * 
	 * @param sc
	 *            a <code>SocketChannel</code> from selector in <code>
	 * {@link ThincProtocol#startEventLoop}</code>
	 * @param bb
	 *            a <code>ByteBuffer</code> containing bytes read from the
	 *            server
	 * @throws ThincException
	 *             if an error occurs while reading bytes
	 */
	protected static int readServerBytes(SocketChannel sc, ByteBuffer bb)
			throws ThincException {
		System.out.println("entering readServerBytes");
		int bytesRead = 0;
		try {
			bytesRead = ThincProtocol.readBytes(sc, bb);
		} catch (IOException e) {
			throw new ThincException("A problem occurred while attempting to "
					+ "read bytes.", e);
		}
		return bytesRead;
	}

	/**
	 * Useful method for reading into a given pixel array. Note that it is
	 * overloaded with an int array version.
	 * 
	 * @param sc
	 *            valid <code>SocketChannel</code> object
	 * @param pixels
	 *            a non-null byte array.
	 * @return number of bytes read into array
	 * @throws ThincException
	 */
	protected int readPixels(SocketChannel sc, byte[] pixels)
			throws ThincException {
		return readServerBytes(sc, proto.wrapByteArray(pixels));
	}

	/**
	 * For reading pixels into an int array. This is to support 24-bit color
	 * handling with the <code>MemoryImageSource</code> object.
	 * 
	 * @param sc
	 *            valid <code>SocketChannel</code> object
	 * @param pixels
	 *            a non-null byte array.
	 * @param offset
	 *            offset in which to start
	 * @return number of bytes read
	 */
	protected int readPixels(SocketChannel sc, int[] pixels, int offset)
			throws ThincException {
		int bytesRead = 0;
		for (int i = 0; i < pixels.length; i++) {
			bytesRead += readServerBytes(sc, buf);
			pixels[offset + i] = buf.asIntBuffer().get();
			buf.clear();
		}
		return bytesRead;
	}

	/**
	 * Method that all <code>ThincServerMessage</code>s must implement.
	 * Should contain the core functionality of any message received from the
	 * server.
	 * 
	 * @throws ThincException
	 *             if there is a problem processing the server message
	 */
	public abstract void processServerMessage(SocketChannel sc)
			throws ThincException;

}
