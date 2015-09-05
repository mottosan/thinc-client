/*********************************************
 * Thinc Client
 * Amortya Ray & Divya Arora
 * {ar2566, da2254} @columbia.edu
 * Fall 2006-Fall 2007
 *********************************************/

package thinc;

public interface ThincMessageTypes {
	/***************************************************************************
	 * server -> client messages *
	 **************************************************************************/

	/** * message types ** */
	final int T_SERV_OK = 1; /* server positive ack */
	final int T_SERV_NOTOK = 2; /* server negative and fatal ack */
	final int T_SERV_SEC_CAPS = 3; /* server security caps */
	final int T_SESS_SEC_CAPS = 4; /* the session security caps */
	final int T_SERV_REPLY = 5; /* server reply to a client request */
	final int T_SERV_LAST = 6; /* just a marker */

	final int T_SSTART = 1; /* thinc_sStart */
	final int T_SPING = 9; /* thinc_sPing */
	final int T_SACK = 10; /* thinc_sAck */

	/* Framebuffer painting messages */
	final int T_FB_INIT = 11; /* thinc_fbInit */
	final int T_FB_RUPDATE = 12; /* thinc_rawUpdate */
	final int T_FB_COPY = 13; /* thinc_fbCopy */
	final int T_FB_SFILL = 14; /* thinc_fbSFill */
	final int T_FB_PFILL = 15; /* thinc_fbPFill */
	final int T_FB_GLYPH = 16; /* thinc_fbGlyph */
	final int T_FB_BILEVEL = 17; /* thinc_fbBilevel */

	/* - 39 reserved for framebuffer operations */
	/* Cursor events */
	final int T_CURSOR_CHANGE = 40; /* thinc_cursorChange */
	final int T_CURSOR_SHOWHIDE = 41; /* thinc_cursorShowHide */
	final int T_CURSOR_MOVE = 42; /* thinc_cursorMove */
	final int T_CURSOR_COLOR = 43; /* thinc_cursorColor */

	/* useful to keep in sync with things that depend on the protocol ids */
	final int T_FB_FIRST_MSG = 12;
	final int T_FB_LAST_MSG = 17;

	/* list of security capabilities */
	final byte T_CAPS_SEC_ENC = 0x0001; /* encryption */
	final byte T_CAPS_SEC_AUTH = 0x0002; /* authentication */

	/* possible byte-order values */
	final int T_BIG = 0;
	final int T_LITTLE = 1;

	/*
	 * current allocations: resize: 50-60 video: 100-110
	 */

	/***************************************************************************
	 * client -> server messages *
	 **************************************************************************/

	/** * message types ** */
	final int T_CLIENT_OK = 1; /* client ack */
	final int T_CLIENT_NOTOK = 2; /* client negative ack */
	final int T_CLIENT_SEC_CAPS = 3; /* client security caps */
	final int T_CLIENT_AUTH = 4; /* client auth info */
	final int T_CLIENT_REQ = 5; /* client handshake request */
	final int T_CLIENT_DONE = 6; /* client is done with handshake */
	final int T_CLIENT_LAST = 7; /* just a marker */

	/* Hacks so we can live with v1 */
	final int T_CAUTH = T_CLIENT_AUTH; // final int T_CAUTH = 2; /* thinc_cAuth
										// */
	final int T_CSTART = 1; /* thinc_cStart */

	/* 2 - 9 reserved for future use */
	final int T_CACK = 10; /* thinc_cAck */
	final int T_EV_MOTION = 11; /* thinc_evMotion */
	final int T_EV_BUTTON = 12; /* thinc_evButton */
	final int T_EV_KEYB = 13; /* thinc_evKeyb */

	/* basic replies */
	final int T_REPLY_OK = 0; /* positive reply */
	final int T_REPLY_NOTOK = 1; /* negative (non-fatal) reply */
	final int T_REPLY_UNKNOWN = 2; /* unknown request */
	final int T_REPLY_LAST = 3; /* marker for requests below */

	/* list of requests */
	final int T_REQ_FBINFO = 3; /* basic framebuffer info */
	final int T_REQ_CURSOR = 4; /* cursor information and data */
	final int T_REQ_FBDATA = 5; /* contents of framebuffer */
	final int T_REQ_ENCODER = 6; /* how is image data encoded */
	final int T_REQ_CACHESZ = 7; /* size of various caches */
	final int T_REQ_VIDEO = 8; /* server supports video? */
	final int T_REQ_NOVIDEO = 9; /*
									 * client telling server that it can't
									 * support video (see below)
									 */
	final int T_REQ_VIDEO_SERV_FMTS = 10; /* video formats supported by server */
	final int T_REQ_VIDEO_CLIENT_FMTS = 11; /*
											 * client informs server of its
											 * video formats
											 */
	final int T_REQ_KEEPALIVE = 12; /*
									 * client supports or not keepalives does
									 * server support them?
									 */
	final int T_REQ_APP_SHARING = 13; /*
										 * does the server support application
										 * sharing?
										 */
	final int T_REQ_APP_LIST = 14; /* list of available applications */
	final int T_REQ_APP_GET = 15; /*
									 * request a particular application. client
									 * may send this message multiple times
									 */
}
