/*********************************************
 * Thinc Client
 * Amortya Ray & Divya Arora
 * {ar2566, da2254} @columbia.edu
 * Fall 2006-Fall 2007
 *********************************************/

package thinc;

// $Id: ThincCMessageKeyEvent.java,v 1.7 2003/07/31 14:37:00 lnk2101 Exp $
// $Source: /proj/ncl/cvsroot/thinc/clients/java/ThincCMessageKeyEvent.java,v $

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.nio.ByteBuffer;

import org.apache.log4j.Logger;
import org.apache.log4j.Logger;

/**
 * ThincCMessageKeyEvent. Handles all keyboard events to server.
 */

final class ThincCMessageKeyEvent extends ThincCMessage implements KeyListener {
	public static Logger logger = ThincLogger
			.getLogger(ThincClientHandShake.class);
	// static constants
	/** The value of the message type is "13" */
	static final byte MESSAGE_TYPE = 13;
	private static final int MESSAGE_LENGTH = 5;
	private static final byte KEY_PRESSED = 1;
	private static final byte KEY_RELEASED = 0;

	// TTY Functions
	private static final int XK_BackSpace = 0xFF08;
	private static final int XK_Tab = 0xFF09;
	private static final int XK_Clear = 0xFF0B;
	private static final int XK_Return = 0xFF0D;
	private static final int XK_Pause = 0xFF13;
	private static final int XK_Scroll_Lock = 0xFF14;
	private static final int XK_Escape = 0xFF1B;
	private static final int XK_Delete = 0xFFFF;

	// Cursor control & motion
	private static final int XK_Home = 0xFF50;
	private static final int XK_Left = 0xFF51;
	private static final int XK_Up = 0xFF52;
	private static final int XK_Right = 0xFF53;
	private static final int XK_Down = 0xFF54;
	private static final int XK_Page_Up = 0xFF55;
	private static final int XK_Page_Down = 0xFF56;
	private static final int XK_End = 0xFF57;

	// Misc Functions
	private static final int XK_Print = 0xFF61;
	private static final int XK_Insert = 0xFF63;
	private static final int XK_Undo = 0xFF65;
	private static final int XK_Redo = 0xFF66;
	private static final int XK_Find = 0xFF68;
	private static final int XK_Cancel = 0xFF69;
	private static final int XK_Help = 0xFF6A;
	private static final int XK_Mode_switch = 0xFF7E;
	private static final int XK_Num_Lock = 0xFF7F;

	// Keypad functions
	private static final int XK_KP_0 = 0xFFB0;
	private static final int XK_KP_1 = 0xFFB1;
	private static final int XK_KP_2 = 0xFFB2;
	private static final int XK_KP_3 = 0xFFB3;
	private static final int XK_KP_4 = 0xFFB4;
	private static final int XK_KP_5 = 0xFFB5;
	private static final int XK_KP_6 = 0xFFB6;
	private static final int XK_KP_7 = 0xFFB7;
	private static final int XK_KP_8 = 0xFFB8;
	private static final int XK_KP_9 = 0xFFB9;

	// function keys
	private static final int XK_F1 = 0xFFBE;
	private static final int XK_F2 = 0xFFBF;
	private static final int XK_F3 = 0xFFC0;
	private static final int XK_F4 = 0xFFC1;
	private static final int XK_F5 = 0xFFC2;
	private static final int XK_F6 = 0xFFC3;
	private static final int XK_F7 = 0xFFC4;
	private static final int XK_F8 = 0xFFC5;
	private static final int XK_F9 = 0xFFC6;
	private static final int XK_F10 = 0xFFC7;
	private static final int XK_F11 = 0xFFC8;
	private static final int XK_F12 = 0xFFC9;
	private static final int XK_F13 = 0xFFCA;
	private static final int XK_F14 = 0xFFCB;
	private static final int XK_F15 = 0xFFCC;
	private static final int XK_F16 = 0xFFCD;
	private static final int XK_F17 = 0xFFCE;
	private static final int XK_F18 = 0xFFCF;
	private static final int XK_F19 = 0xFFD0;
	private static final int XK_F20 = 0xFFD1;
	private static final int XK_F21 = 0xFFD2;
	private static final int XK_F22 = 0xFFD3;
	private static final int XK_F23 = 0xFFD4;
	private static final int XK_F24 = 0xFFD5;

	// modifiers
	private static final int XK_Shift_L = 0xFFE1; /* Left shift */
	// private static final int XK_Shift_R = 0xFFE2; /* Right shift */
	private static final int XK_Control_L = 0xFFE3; /* Left control */
	// private static final int XK_Control_R = 0xFFE4; /* Right control */
	private static final int XK_Caps_Lock = 0xFFE5; /* Caps lock */
	private static final int XK_Shift_Lock = 0xFFE6; /* Shift lock */

	private static final int XK_Meta_L = 0xFFE7; /* Left meta */
	// private static final int XK_Meta_R = 0xFFE8; /* Right meta */
	private static final int XK_Alt_L = 0xFFE9; /* Left alt */
	// private static final int XK_Alt_R = 0xFFEA; /* Right alt */

	private static final int XK_Semi_Colon = 0x003D;
	private static final int VK_SEMICOLON = 0x00CD;

	private static final int XK_Colon = 0x003B;
	private static final int VK_COLON = 0x00BA;

	private static final int XK_Single_Quote = 0x0027;
	private static final int VK_SINGLE_QUOTE = 0x00DE;

	private static final int XK_Back_Tick = 0x0060;
	private static final int VK_BACK_TICK = 0X00C0;

	private static final int XK_Back_slash = 0x005C;
	private static final int VK_BACK_SLASH = 0x00DC;

	private static final int XK_Period = 0x002E;
	private static final int VK_PERIOD = 0x00BE;

	private static final int XK_Slash = 0x002F;
	private static final int VK_SLASH = 0x00BF;

	private static final int XK_Comma = 0x002C;
	private static final int VK_COMMA = 0x00BC;

	private static final int XK_RBracket = 0x005B;
	private static final int VK_RBRACKET = 0x00DB;

	private static final int XK_LBracket = 0x005D;
	private static final int VK_LBRACKET = 0x00DD;

	private static final int XK_Plus = 0x003D;
	private static final int VK_PLUS = 0x00BB;

	private static final int XK_Minus = 0x002D;
	private static final int VK_MINUS = 0x00BD;

	private static final int XKEY_SIZE = 98;

	// GLYPHS_PER_KEY 4
	// NUM_KEYCODES 248
	// MIN_KEYCODE 8
	// MAX_KEYCODE = NUM_KEYCODES + MIN_KEYCODE -1
	// define CONTROL_L_CODE (MIN_KEYCODE + 29)
	// define CONTROL_R_CODE (MIN_KEYCODE + 101)
	// define SHIFT_L_CODE (MIN_KEYCODE + 42)
	// define SHIFT_R_CODE (MIN_KEYCODE + 54)
	// define META_L_CODE (MIN_KEYCODE + 107)
	// define META_R_CODE (MIN_KEYCODE + 108)
	// define ALT_L_CODE (MIN_KEYCODE + 56)
	// define ALT_R_CODE (MIN_KEYCODE + 105)

	/**
	 * Prepares this message to be sent to the server. Sets the message type
	 * header and the version string.
	 * 
	 * @param proto
	 *            a valid <code>ThincProtocol</code> object
	 */
	ThincCMessageKeyEvent(ThincProtocol proto) {
		super(proto, MESSAGE_TYPE, MESSAGE_LENGTH);
	}

	private void setKeyEventAndSend(KeyEvent ke, byte down) {
		logger.debug("Entering setKeyEventAndSend in ThincCMessageKeyEvent");
		// System.out.println("Got key " + key + ", value '" + down + "'.");
		ByteBuffer bb = retrieveMessageByteBuffer();
		// System.out.println("key pressed: "+ke.getKeyCode()+"
		// kc:"+ke.getKeyChar());
		bb.putInt(getKeyCode(ke));
		// bb.putInt(ke.getKeyCode());
		bb.put(down);
		bb.flip();
		try {
			// System.out.println("Sending key event message.");
			sendClientMessage(bb);
		} catch (ThincException e) {
			// NO-OP
			System.out.println("Exception occurred while handling key event "
					+ "event: " + e.toString());
			// :TODO: PUT A RUNTIME EXCEPTION HERE?
		}
		bb.clear();
		logger.debug("Exiting setKeyEventAndSend in ThincCMessageKeyEvent");
	}

	private int getKeyCode(KeyEvent e) {
		logger.debug("Entering getKeyCode in ThincCMessageKeyEvent");
		int keyCode = e.getKeyCode();
		switch (keyCode) {
		case KeyEvent.VK_BACK_SPACE:
			keyCode = XK_BackSpace;
			break;
		case KeyEvent.VK_TAB:
			keyCode = XK_Tab;
			break;
		case KeyEvent.VK_CLEAR:
			keyCode = XK_Clear;
			break;
		case KeyEvent.VK_ENTER:
			keyCode = XK_Return;
			break;
		case KeyEvent.VK_PAUSE:
			keyCode = XK_Pause;
			break;
		case KeyEvent.VK_SCROLL_LOCK:
			keyCode = XK_Scroll_Lock;
			break;
		case KeyEvent.VK_ESCAPE:
			keyCode = XK_Escape;
			break;
		case KeyEvent.VK_DELETE:
			keyCode = XK_Delete;
			break;

		case KeyEvent.VK_HOME:
			keyCode = XK_Home;
			break;
		case KeyEvent.VK_LEFT:
			keyCode = XK_Left;
			break;
		// case KeyEvent.VK_LEFT: keyCode = 0x005c; break;
		case KeyEvent.VK_RIGHT:
			keyCode = XK_Right;
			break;
		case KeyEvent.VK_UP:
			keyCode = XK_Up;
			break;
		case KeyEvent.VK_DOWN:
			keyCode = XK_Down;
			break;
		case KeyEvent.VK_PAGE_UP:
			keyCode = XK_Page_Up;
			break;
		case KeyEvent.VK_PAGE_DOWN:
			keyCode = XK_Page_Down;
			break;
		case KeyEvent.VK_END:
			keyCode = XK_End;
			break;

		case KeyEvent.VK_PRINTSCREEN:
			keyCode = XK_Print;
			break;
		case KeyEvent.VK_INSERT:
			keyCode = XK_Insert;
			break;
		case KeyEvent.VK_UNDO:
			keyCode = XK_Undo;
			break;
		case KeyEvent.VK_AGAIN:
			keyCode = XK_Redo;
			break;
		case KeyEvent.VK_FIND:
			keyCode = XK_Find;
			break;
		case KeyEvent.VK_CANCEL:
			keyCode = XK_Cancel;
			break;
		case KeyEvent.VK_HELP:
			keyCode = XK_Help;
			break;
		case KeyEvent.VK_MODECHANGE:
			keyCode = XK_Mode_switch;
			break;
		case KeyEvent.VK_NUM_LOCK:
			keyCode = XK_Num_Lock;
			break;

		case KeyEvent.VK_NUMPAD0:
			keyCode = XK_KP_0;
			break;
		case KeyEvent.VK_NUMPAD1:
			keyCode = XK_KP_1;
			break;
		case KeyEvent.VK_NUMPAD2:
			keyCode = XK_KP_2;
			break;
		case KeyEvent.VK_NUMPAD3:
			keyCode = XK_KP_3;
			break;
		case KeyEvent.VK_NUMPAD4:
			keyCode = XK_KP_4;
			break;
		case KeyEvent.VK_NUMPAD5:
			keyCode = XK_KP_5;
			break;
		case KeyEvent.VK_NUMPAD6:
			keyCode = XK_KP_6;
			break;
		case KeyEvent.VK_NUMPAD7:
			keyCode = XK_KP_7;
			break;
		case KeyEvent.VK_NUMPAD8:
			keyCode = XK_KP_8;
			break;
		case KeyEvent.VK_NUMPAD9:
			keyCode = XK_KP_9;
			break;

		case KeyEvent.VK_F1:
			keyCode = XK_F1;
			break;
		case KeyEvent.VK_F2:
			keyCode = XK_F2;
			break;
		case KeyEvent.VK_F3:
			keyCode = XK_F3;
			break;
		case KeyEvent.VK_F4:
			keyCode = XK_F4;
			break;
		case KeyEvent.VK_F5:
			keyCode = XK_F5;
			break;
		case KeyEvent.VK_F6:
			keyCode = XK_F6;
			break;
		case KeyEvent.VK_F7:
			keyCode = XK_F7;
			break;
		case KeyEvent.VK_F8:
			keyCode = XK_F8;
			break;
		case KeyEvent.VK_F9:
			keyCode = XK_F9;
			break;
		case KeyEvent.VK_F10:
			keyCode = XK_F10;
			break;
		case KeyEvent.VK_F11:
			keyCode = XK_F11;
			break;
		case KeyEvent.VK_F12:
			keyCode = XK_F12;
			break;
		case KeyEvent.VK_F13:
			keyCode = XK_F13;
			break;
		case KeyEvent.VK_F14:
			keyCode = XK_F14;
			break;
		case KeyEvent.VK_F15:
			keyCode = XK_F15;
			break;
		case KeyEvent.VK_F16:
			keyCode = XK_F16;
			break;
		case KeyEvent.VK_F17:
			keyCode = XK_F17;
			break;
		case KeyEvent.VK_F18:
			keyCode = XK_F18;
			break;
		case KeyEvent.VK_F19:
			keyCode = XK_F19;
			break;
		case KeyEvent.VK_F20:
			keyCode = XK_F20;
			break;
		case KeyEvent.VK_F21:
			keyCode = XK_F21;
			break;
		case KeyEvent.VK_F22:
			keyCode = XK_F22;
			break;
		case KeyEvent.VK_F23:
			keyCode = XK_F23;
			break;
		case KeyEvent.VK_F24:
			keyCode = XK_F24;
			break;

		case KeyEvent.VK_SHIFT:
			keyCode = XK_Shift_L;
			break;
		case KeyEvent.VK_CONTROL:
			keyCode = XK_Control_L;
			break;
		case KeyEvent.VK_CAPS_LOCK:
			keyCode = XK_Caps_Lock;
			break;

		case KeyEvent.VK_META:
			keyCode = XK_Meta_L;
			break;
		case KeyEvent.VK_ALT:
			keyCode = XK_Alt_L;
			break;
		case KeyEvent.VK_QUOTE:
			keyCode = XK_Single_Quote;
			break;
		default:
			if (keyCode > 64 && keyCode < 91)
				return keyCode + 32;
			break;
		// default: System.out.println("Unmapped KeyEvent: " + keyCode);
		}
		logger.debug("Exiting getKeyCode in ThincCMessageKeyEvent");
		return keyCode;
	}

	// ------------------------- KeyListener methods -------------------------//
	public void keyPressed(KeyEvent e) {
		logger.debug("Entering keyPressed in ThincCMessageKeyEvent");
		setKeyEventAndSend(e, KEY_PRESSED);

	}

	public void keyReleased(KeyEvent e) {
		logger.debug("Entering keyReleased in ThincCMessageKeyEvent");
		setKeyEventAndSend(e, KEY_RELEASED);
	}

	public void keyTyped(KeyEvent e) {
		logger.debug("Entering keyTyped in ThincCMessageKeyEvent");
		// sendKeyPressed(e);
	}
}
