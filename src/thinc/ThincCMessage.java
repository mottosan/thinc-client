/*********************************************
 * Thinc Client
 * Amortya Ray & Divya Arora
 * {ar2566, da2254} @columbia.edu
 * Fall 2006-Fall 2007
 *********************************************/

package thinc;

// $Id: ThincCMessage.java,v 1.10 2003/11/08 00:56:34 lnk2101 Exp $
// $Source: /proj/ncl/cvsroot/thinc/clients/java/ThincCMessage.java,v $

import java.io.IOException;
import java.nio.ByteBuffer;
import org.apache.log4j.Logger;

/**
 * ThincCMessage interface. All classes that represent client-to-server messages
 * must implement this interface.
 */

abstract class ThincCMessage {
	public static Logger logger = ThincLogger.getLogger(ThincCMessage.class);
	// static constants
	private static final int HEADER_LENGTH = 4;

	// member variables
	ThincProtocol proto;
	private ByteBuffer bb;
	private byte messageType = -1;

	protected ThincCMessage() {
		// no instantiation without args
	}

	protected ThincCMessage(ThincProtocol proto, byte messageType,
			int messageLength) {
		this.proto = proto;
		this.messageType = messageType;
		this.bb = proto.allocateHeaderByteBuffer(messageLength + HEADER_LENGTH);
	}

	/**
	 * Retrieves the message <code>ByteBuffer</code>, which contains the
	 * current message's <code>MESSAGE_TYPE</code> byte. Every time this
	 * method is invoked, the <code> MESSAGE_TYPE</code> of the current message
	 * is inserted. Always call the "clear" method on the returned
	 * <code>ByteBuffer</code> after using it.
	 * 
	 * @return a <code>ByteBuffer</code> that contains a byte representing the
	 *         message type plus enough space allocated for the actual message
	 */
	protected ByteBuffer retrieveMessageByteBuffer() {
		logger.debug("Entering retrieveMessageByteBuffer within ThincCMessage");
		bb.put(messageType);
		// entering garbage message flag data
		bb.put((byte) 0);
		// entering garbage message length data
		bb.putShort((short) 0);
		logger.debug("Exiting retrieveMessageByteBuffer within ThincCMessage");
		return bb;
	}

	/**
	 * Sends passed-in <code>ByteBuffer</code> to server.
	 * 
	 * @param bb
	 *            a <code>ByteBuffer</code> representing message to be sent
	 * @throws ThincException
	 *             if there is a problem sending the message
	 */
	protected int sendClientMessage(ByteBuffer bb) throws ThincException {
		logger.debug("Entering sendClientMessage within ThincCMessage");
		// send bytes
		int bytesSent = 0;
		try {
			System.out.println(" Sending: " + bb.toString());
			bytesSent = ThincProtocol.sendBytes(proto.getSocketChannel(), bb);
		} catch (IOException e) {
			throw new ThincException("An exception occurred in "
					+ getClass().getName() + " while sending message to "
					+ "server.", e);
		}
		logger.debug("Exiting sendClientMessage within ThincCMessage");
		return bytesSent;
	}

}