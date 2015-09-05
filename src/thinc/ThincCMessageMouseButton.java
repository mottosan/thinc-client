/*********************************************
 * Thinc Client
 * Amortya Ray & Divya Arora
 * {ar2566, da2254} @columbia.edu
 * Fall 2006-Fall 2007
 *********************************************/

package thinc;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.nio.ByteBuffer;

/**
 * ThincCMessageMouseButton. Implements the <code>MouseListener</code>
 * interface and is added as a listener to the <code>ThincClientCanvas</code>.
 * 
 * @see MouseListener
 */

final class ThincCMessageMouseButton extends ThincCMessage implements
		MouseListener {
	// static constants
	/** The value of the message type is "12" */
	static final byte MESSAGE_TYPE = 12;
	private static final int MESSAGE_LENGTH = 1;
	private static final boolean BUTTON_PRESSED = true;
	private static final boolean BUTTON_RELEASED = false;

	/**
	 * Prepares this message to be sent to the server. Sets the message type
	 * header and the version string.
	 * 
	 * @param proto
	 *            a valid <code>ThincProtocol</code> object
	 */
	ThincCMessageMouseButton(ThincProtocol proto) {
		super(proto, MESSAGE_TYPE, MESSAGE_LENGTH);
	}

	/**
	 * Sets the button mask for this event.
	 * 
	 * @param me
	 *            a <code>MouseEvent</code> object
	 * @param down
	 *            a <code>boolean</code> where "true" means button pressed and
	 *            "false" means button released.
	 */
	private void setButtonMaskAndSend(MouseEvent me, boolean down) {
		ByteBuffer bb = retrieveMessageByteBuffer();
		byte buttonMask = (byte) me.getButton();
		if (down)
			buttonMask |= 128;
		bb.put(buttonMask);
		bb.flip();
		try {
			sendClientMessage(bb);
		} catch (ThincException e) {
			// NO-OP
			System.out
					.println("Exception occurred while handling button event "
							+ "event: " + e.toString());
			// :TODO: PUT A RUNTIME EXCEPTION HERE?
		}
		bb.clear();
	}

	// ------------------------ MouseListener methods
	// -------------------------//
	/**
	 * Note that this method is a NO-OP. Mouse "clicked" events are to coarse
	 * for the THINC client to handle.
	 */
	public void mouseClicked(MouseEvent e) {
	}

	/**
	 * Note that this method is a NO-OP.
	 */
	public void mouseEntered(MouseEvent e) {
	}

	/**
	 * Note that this method is a NO-OP.
	 */
	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
		// System.out.println("Button pressed.");
		setButtonMaskAndSend(e, BUTTON_PRESSED);
	}

	public void mouseReleased(MouseEvent e) {
		// System.out.println("Button released.");
		setButtonMaskAndSend(e, BUTTON_RELEASED);
	}
}
