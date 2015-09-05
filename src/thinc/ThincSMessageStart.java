/*********************************************
 * Thinc Client
 * Amortya Ray & Divya Arora
 * {ar2566, da2254} @columbia.edu
 * Fall 2006-Fall 2007
 *********************************************/

package thinc;

import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;

/**
 * ThincSMessageStart class. Handles initial version message sent by server.
 */

final class ThincSMessageStart extends ThincSMessageBase {

	// static constants
	/** Value of this is "1" */
	static final byte MESSAGE_TYPE = 1;
	private static final int MESSAGE_LENGTH = 0;

	ThincSMessageStart(ThincProtocol proto) {
		super(proto, MESSAGE_LENGTH + 4);
	}

	/**
	 * Reads the byte order and the version of the server.
	 * 
	 * @param sc
	 *            a <code>SocketChannel</code> from the select in <code>
	 * {@link ThincProtocol#startEventLoop}</code>
	 * @throws ThincException
	 *             if the version of the server is incompatible with the version
	 *             of the client
	 */
	public void processServerMessage(SocketChannel sc) throws ThincException {
		readServerBytes(sc, bb);
		ThincProtocol.debug("Reading handshake message.");
		// flip the buffer
		bb.flip();
		System.out.println("message:");
		for (int i = 0; i < bb.limit(); ++i) {
			System.out.print(bb.get(i) + " ");
		}
		System.out.println();
		// discard message type
		bb.get();

		// discard flags
		bb.get();

		// discard length
		bb.getShort();

		// then read byteorder
		byte byteOrder = bb.get();

		// then read version string. it should ALWAYS be three bytes.
		byte[] version = new byte[3];
		bb.get(version);

		// set byte order on protocol object
		// we check to see if the native byte order matches the byte order of
		// the server. if so, we use that because it is more efficient.
		// otherwise we just set the byte order appropriately.
		proto.defaultByteOrder = (byteOrder == 1)
		// little-endian
		? (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN) ? ByteOrder
				.nativeOrder() : ByteOrder.LITTLE_ENDIAN)
				// big-endian
				: (ByteOrder.nativeOrder().equals(ByteOrder.BIG_ENDIAN) ? ByteOrder
						.nativeOrder()
						: ByteOrder.BIG_ENDIAN);

		System.out.println("!!!!!!!!!!LOOK AT ME!!!!!!!!");
		System.out.println(version.length + " "
				+ ThincProtocol.CLIENT_VERSION.length);
		for (int i = 0; i < ThincProtocol.CLIENT_VERSION.length; i++) {
			System.out.println(version[i] + " "
					+ ThincProtocol.CLIENT_VERSION[i]);
		}

		// handle the version
		for (int i = 0; i < ThincProtocol.CLIENT_VERSION.length; i++) {
			if (ThincProtocol.CLIENT_VERSION[i] != version[i]) {
				System.out
						.println(version + " " + ThincProtocol.CLIENT_VERSION);
				// :TODO: better exception here? perhaps a ThincServerException?
				throw new ThincException("Incompatible version found. Version "
						+ "required by client: "
						+ getStringFromBytes(ThincProtocol.CLIENT_VERSION)
						+ ", version of server: " + getStringFromBytes(version));
			}
		}

		bb.clear();
	}

	private String getStringFromBytes(byte[] b) {
		StringBuffer strBuf = new StringBuffer();
		for (int i = 0; i < b.length; i++) {
			strBuf.append(b[i]);
		}
		return strBuf.toString();
	}
}
