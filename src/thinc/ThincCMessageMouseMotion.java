/*********************************************
 * Thinc Client
 * Amortya Ray & Divya Arora
 * {ar2566, da2254} @columbia.edu
 * Fall 2006-Fall 2007
 *********************************************/

package thinc;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.nio.ByteBuffer;

/**
 * ThincCMessageMouseMotion class. Sends the version string, which is held in
 * <code>{@link ThincProtocol}</code>.
 * 
 * @see MouseMotionListener
 */

final class ThincCMessageMouseMotion extends ThincCMessage implements
		MouseMotionListener {
	// static constants
	/** The value of the message type is "11" */
	static final byte MESSAGE_TYPE = 11;
	private static final int MESSAGE_LENGTH = 4;

	/**
	 * Prepares this message to be sent to the server. Sets the message type
	 * header and the version string.
	 * 
	 * @param proto
	 *            a valid <code>ThincProtocol</code> object
	 */
	ThincCMessageMouseMotion(ThincProtocol proto) {
		super(proto, MESSAGE_TYPE, MESSAGE_LENGTH);
	}

	/**
	 * Sets the mouse coordinates.
	 * 
	 * @param x
	 *            an <code>int</code> representing the x-coordinate
	 * @param y
	 *            an <code>int</code> representing the y-coordinate
	 */
	private void setCoordinatesAndSend(int x, int y) {
		ByteBuffer bb = retrieveMessageByteBuffer();
		bb.putShort((short) x);
		bb.putShort((short) y);
		bb.flip();
		try {
			sendClientMessage(bb);
		} catch (ThincException e) {
			// NO-OP
			System.out
					.println("Exception occurred while handling mouse motion "
							+ "event: " + e.toString());
			// :TODO: PUT A RUNTIME EXCEPTION HERE?
		}
		bb.clear();
	}

	// --------------------- MouseMotionListener methods
	// ----------------------//
	public void mouseDragged(MouseEvent e) {
		// e = compressMouseMotion(e);
		setCoordinatesAndSend(e.getX(), e.getY());
	}

	public void mouseMoved(MouseEvent e) {
		// e = compressMouseMotion(e);
		setCoordinatesAndSend(e.getX(), e.getY());
	}

	private static MouseEvent compressMouseMotion(MouseEvent e) {
		ThincClientCanvas c = (ThincClientCanvas) e.getSource();
		EventQueue queue = c.getToolkit().getSystemEventQueue();
		AWTEvent event;
		try {
			while ((event = queue.getNextEvent()) instanceof MouseEvent) {
				ThincProtocol.debug("Compressing mouse event: " + e);
				e = (MouseEvent) event;
				if (null == queue.peekEvent()
						|| !(queue.peekEvent() instanceof MouseEvent)) {
					break;
				}
			}
		} catch (InterruptedException ex) {
			ThincProtocol.debug("Queue interrupted.");
		}
		ThincProtocol.debug("Returned mouse event: " + e);
		return e;
	}

}