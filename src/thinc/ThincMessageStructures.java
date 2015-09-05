/*********************************************
 * Thinc Client
 * Amortya Ray & Divya Arora
 * {ar2566, da2254} @columbia.edu
 * Fall 2006-Fall 2007
 *********************************************/

package thinc;

public interface ThincMessageStructures {
	// final byte[] THINC_VERSION = new byte[]
	// {'T','H','I','N','C','_','0','.','2','\0'};

	/** ************************** Message header ***************************** */
	public class thinc_header { // (thinc_message_header)
		public static byte type; // :8 8 bits seems more than enough. If we
									// have more
		public static byte flags; // :8 /* message dependent */
		public static short length; // :16 /* length of the message.

		thinc_header(int t, int f, int l) {
			type = (byte) t;
			flags = (byte) f;
			length = (short) l;

		}
	}

	final int THINC_HEADER_SIZE = 4; // in C THIN_HEADERSIZE

	// ///////////////////////////////////////////
	/** * message definitions ** */
	/*
	 * T_SERV_SEC_CAPS Description: The security capabilities of the server. See
	 * above for available capabilities Size: 4 Data: None
	 */
	public class thinc_serverSecCaps {
		static int caps; // :32 /* 1 bit for each capability */
	}

	final int T_SERV_SEC_CAPS_SIZE = 4;

	/*
	 * T_SESS_SEC_CAPS Description: The security capabilities for the session.
	 * Size: 4 Data: None
	 */
	public class thinc_sessionSecCaps {
		static int caps; // :32 /* 1 bit for each capability */
	}

	final int T_SESS_SEC_CAPS_SIZE = 4;

	/*
	 * T_CLIENT_REQ Description: A handshake request from the client. Identifies
	 * and precedes the specific request being made. You should think of it not
	 * as the client asking for something from the server, but as the beginning
	 * of a negotiation with the server, as requests can carry client
	 * information too, or they may just be used by the client to inform the
	 * server of "something" (in which case the server will just reply "OK")
	 * Size: 4 Data: None
	 */
	public class thinc_clientReq {
		public static int request; // :16
		public static int unused; // :16
	}

	final int T_CLIENT_REQ_SIZE = 4;

	/*
	 * T_SERV_REPLY Description: A reply to a client request. The id may be
	 * either one of the thinc_serverReplies below or the id of the client's
	 * request to which we're replying. It may wrap around an additional reply
	 * message containing data needed for the reply. IMPORTANT: Use the
	 * header.length field so that client can read the whole reply easily
	 * without having to figure out which reply you sent first Size: 4 Data:
	 * None
	 */
	public class thinc_serverReply {
		static short reply; // :16;/* id of the reply */
		static short unused; // :16;
	}

	final int T_SERV_REPLY_SIZE = 4;

	/*
	 * request and reply messages. for requests, first member _must_ be
	 * thinc_clientReq. for replies first member _must_ be thinc_serverReply.
	 * use t_request_head and t_reply_head for this BIG ASSUMPTION: compiler
	 * won't add pad after this struct since it's aligned to 4-byte boundary.
	 * 
	 * The *_SIZE final ints do not include the size of the reply/request
	 * header. use t_reply_size() and t_req_size() to compute the total size if
	 * needed
	 */

	/* simple replies */
	public class thinc_replyOK {
		thinc_serverReply reply;
	}

	final int T_REPLY_OK_SIZE = 0;

	public class thinc_replyNotOK {
		thinc_serverReply reply;
	}

	final int T_REPLY_NOTOK_SIZE = 0;

	public class thinc_replyUnknown {
		thinc_serverReply reply;
	}

	final int T_REPLY_UNKNOWN_SIZE = 0;

	/** **** FBINFO ***************** */
	public class thinc_reqFBInfo {
		thinc_clientReq req;
	}

	final int T_REQ_FBINFO_SIZE = 0;

	public class thinc_replyFBInfo {
		// ???? public thinc_serverReply reply;
		static int reply_head;
		static short byteOrderMbz; // byteOrder:1 /* T_BIG | T_LITTLE
									// (thincProto.h) */
		// mbz:15 /*reserved for future use */
		static byte depth; // :8
		static byte bpp; // :8 /* bits per pixel */
		static short width; // :16
		static short height; // :16
	}

	final int T_REPLY_FBINFO_SIZE = 8;

	/** **** CURSOR ***************** */
	public class thinc_reqCursor {
		thinc_clientReq req;
	}

	final int T_REQ_CURSOR_SIZE = 0;

	public class thinc_replyCursor {
		// thinc_serverReply reply;
		static int reply_head;
		static short flags; // :16; /* for now just: is it argb or not */
		static short size; // :16; /* size of cursor data (bytes) */
		static byte width; // :8; /* dimensions */
		static byte height; // :8;
		static byte xhot; // :8; /* hot spot */
		static byte yhot; // :8;
		static short x; // :16;/* position */
		static short y; // :16;
		static int fg; // :32;/* colors. ignore if ARGB */
		static int bg; // :32;
		/* followed by size bytes representing the cursor */
	}

	final int T_REPLY_CURSOR_SIZE = 20;

	/* flags */
	/*
	 * reuse from cursor change message final int T_CURSOR_ARGB 0x01
	 */

	/** **** ENCODER **************** */
	public class thinc_reqEncoder {
		thinc_clientReq req;
	}

	final int T_REQ_ENCODER_SIZE = 0;

	public class thinc_replyEncoder {
		// thinc_serverReply reply;
		static int reply_head;
		// int which :2; /* zlib, png, none. see below */
		// int reserved :30; /* reserved for future use */
		static int whichReserved; // which:2, reserved:30
	}

	final int T_REPLY_ENCODER_SIZE = 4;

	final byte T_ENCODER_NONE = 0x00;
	final byte T_ENCODER_PNG = 0x01;
	final byte T_ENCODER_ZLIB = 0x02;

	/** **** FBDATA ****************** */
	public class thinc_reqFBData {
		thinc_clientReq req;
	}

	final int T_REQ_FBDATA_SIZE = 0;

	public class thinc_replyFBData {
		// static thinc_serverReply reply;
		static int reply_head;
		static int flags; // :32;
		/*
		 * if compressed, followed by fbZData. if not, followed by pixel data
		 */
	}

	final int T_REPLY_FBDATA_SIZE = 4;

	/*
	 * (already in thincProto.h) final int T_FB_COMPRESSED 0x01
	 */

	/*
	 * Support for compressed initial framebuffer: When FB_INIT is sent, the
	 * following flag will be on in the flags field of the header. The
	 * framebuffer will be sent by compressing each scanline, and wrapping it in
	 * a T_FB_ZDATA message which gives the client enough information to
	 * uncompress it appropiately.
	 */
	final byte T_FB_COMPRESSED = 0x01; /* in flags field of the header */
	final byte T_FB_CURSOR_ARGB = 0x02; /*
										 * the cursor is argb. cursorFG and
										 * cursorBG should be ignored, and
										 * cursorSize is the total data size in
										 * bytes of pixel data of the cursor
										 */

	/*
	 * T_FB_ZDATA Description: Wrapper for a compressed update. It contains the
	 * compressed and uncompressed size of the data following it. Size: 4 bytes
	 * Data: zsize bytes of compressed data
	 */
	public class thinc_fbZData {
		static int zsize; // :32;/* length of the compressed scanline */
		static int usize; // :32;/* length of the uncompressed scanline */
	}

	final int T_FB_ZDATA_SIZE = 8;

	/** **** CACHESIZE ************** */
	/* TODO: Should the client tell the server which caches it supports? */
	public class thinc_reqCacheSz {
		thinc_clientReq req;
	}

	final int T_REQ_CACHESZ_SIZE = 0;

	public class thinc_replyCacheSz {
		// thinc_serverReply reply;
		static int reply_head;
		/* all cache sizes are in bits */
		static byte img; // :8;
		static byte bit; // :8;
		static byte pix; // :8;
		static byte reserved; // :8;
	}

	final int T_REPLY_CACHESZ_SIZE = 4;

	/** **** VIDEO ****************** */
	/* server replies ok or notok */
	public class thinc_reqVideo {
		thinc_clientReq req;
	}

	final int T_REQ_VIDEO_SIZE = 0;

	/* server doesn't reply (unless he or she feels insulted) */
	public class thinc_reqNoVideo {
		thinc_clientReq req;
	}

	final int T_REQ_NOVIDEO_SIZE = 0;

	public class thinc_reqVideoServFmts {
		thinc_clientReq req;
	}

	final int T_REQ_VIDEO_SERV_FMTS_SIZE = 0;

	// public class thinc_replyVideoServFmts {
	// thinc_serverReply reply;
	// thinc_vidSInit fmts; /* from thincProtoVideo.h */
	// }
	// final int T_REPLY_VIDEO_SERV_FMTS_SIZE = T_VIDEO_SINIT_SIZE;
	//
	// /* server replies ok or notok */
	// public class thinc_reqVideoClientFmts {
	// thinc_clientReq req;
	// thinc_vidCInit fmts; /* from thincProtoVideo.h */
	// }
	// final int T_REQ_VIDEO_CLIENT_FMTS_SIZE = T_VIDEO_CINIT_SIZE;

	/** **** APPLICATION SHARING *** */
	/*
	 * T_REQ_APP_SHARING server replies ok or notok. If server replies ok (see
	 * thincProtoApp.h) each thinc_header sent will be followed by a
	 * thinc_appHeader
	 */
	public class thinc_reqAppSharing {
		thinc_clientReq req;
	}

	final int T_REQ_APP_SHARING_SIZE = 0;

	/*
	 * T_REQ_APPLIST client asks the server which applications can be shared.
	 * The idea is to let the user choose which ones it wants.
	 */
	public class thinc_reqAppList {
		thinc_clientReq req;
	}

	final int T_REQ_APP_LIST_SIZE = 0;

	/* how many apps we're sending, followed by count thinc_appDesc structs */
	public class thinc_replyAppList {
		thinc_serverReply reply;
		byte count; // :8;
		byte mbz; // :8;
	}

	final int T_REPLY_APP_LIST_SIZE = 2;

	/*
	 * T_REQ_APPLIST ask the server for a particular app (from the list sent by
	 * APP_LIST). The server may reply notok...
	 */
	public class thinc_reqAppGet {
		thinc_clientReq req;
		byte app_id; // :8;
		byte mbz; // :8;
	}

	final int T_REQ_APP_GET_SIZE = 2;

	public class thinc_replyAppGet {
		thinc_serverReply reply;
	}

	final int T_REPLY_APP_GET_SIZE = 0;

	// ///////////////////////////////////////////
	/*
	 * T_SERV_OK Description: A good reply from the server. Size: 0 Data: None
	 */
	/** * no need for struct, just the type in the header is enough ** */
	final int T_SERV_OK_SIZE = 0;

	/*
	 * T_SERV_NOTOK Description: A negative ack from the server. After this is
	 * sent, the connection should be closed. Size: 4 Data: (Optional) reason
	 * for NOTOK, plaintext string
	 */
	public class thinc_serverNotOk {
		static int reason_len; // :32; /* if 0, no reason */
		/* possibly followed by string describing reason for reject */
	}

	final int T_SERV_NOTOK_SIZE = 4;

	/** * message definitions ** */

	/*
	 * T_CLIENT_SEC_CAPS Description: The security capabilities of the client.
	 * See above for available capabilities Size: 4 Data: None
	 */
	public class thinc_clientSecCaps {
		static int caps; // :32 /* 1 bit for each capability */
	}

	final int T_CLIENT_SEC_CAPS_SIZE = 4;

	/*
	 * T_CLIENT_NOTOK Description: A negative reply from the client. After this
	 * is sent, the connection should be closed. Size: 4 Data: (Optional) reason
	 * for NOTOK, plaintext string
	 */
	public class thinc_clientNotOk {
		int reason_len; // :32 /* if 0, no reason */
		/* possibly followed by string describing reason for reject */
	}

	final int T_CLIENT_NOTOK_SIZE = 4;

	/*
	 * T_CLIENT_OK Description: Positive reply from client. Size: 0 Data: None
	 */
	/** * no need for struct, just the type in the header is enough ** */
	final int T_CLIENT_OK_SIZE = 0;

	/*
	 * T_CLIENT_DONE Description: Tells server that the handshake is done Size:
	 * 0 Data: None
	 */
	/** * no need for struct, just the type in the header is enough */
	final int T_CLIENT_DONE_SIZE = 0;

	/***************************************************************************
	 * session features negotiation *
	 **************************************************************************/

	// //////////////////////////////////////////////////////////////////
	/** ******************** helper structures ***************************** */
	/* description of a rectangular region */
	public class thinc_rectRegion {
		static short x; // :16
		static short y; // :16
		static short width; // :16
		static short height; // :16
	}

	final int T_RECTREGION_SIZE = 8;

	/** ************************** The messages ****************************** */
	public class thinc_sStart {
		byte byte_order; // :8
		char version[] = new char[3]; // :char(3)
	}

	final int T_SSTART_SIZE = 4;

	/*
	 * T_SPING Description: kind of a keep alive. The message doesn't carry
	 * anything beyond the header. The purpose is to try and keep the server to
	 * client link from getting idle. Size: 0 Data: NONE
	 */
	final int T_SPING_SIZE = 0;

	/*
	 * T_SACK Description: Used by the server to acknowledge certain messages.
	 * It's a 1 or 0, representing the server's acceptance or denial to a
	 * particular client message. Size: 1 Data: NONE
	 */
	public class thinc_sAck {
		static byte ack; // :8
		static char pad[] = new char[3]; // char(3)
	}

	final int T_SACK_SIZE = 1;

	/*
	 * T_FB_RUPDATE Description: Message sent for a raw rectangular frame buffer
	 * update. Carries the description of the location and size of the update.
	 * Size: 8 bytes Data: Followed by height lines of size width*bpp,
	 * representing the new contents of the described frame buffer region
	 */
	public class thinc_rawUpdate {
		static short x; // :16;/* coords of top-left corner of the update */
		static short y; // :16;
		static short width; // :16;/* dimensions of the update */
		static short height; // :16;
	}

	final int T_FB_RUPDATE_SIZE = 8;

	/* flags *//* order */
	final byte T_FB_RUPDATE_COMPRESSED = 0x01; /* 3 */
	final byte T_FB_RUPDATE_RESIZED = 0x02; /* 2 */
	final byte T_FB_RUPDATE_CACHED = 0x04; /* 1 */
	final byte T_FB_RUPDATE_ADDCACHE = 0x08; /* 1 */

	/*
	 * T_FB_COPY Description: Tells the client to copy the area of size
	 * width*height, with top left corner sx,sy to dx,dy. Size: 12 bytes Data:
	 * NONE
	 */
	public class thinc_fbCopy {
		static short sx; // :16;
		static short sy; // :16;
		static short dx; // :16;
		static short dy; // :16;
		static short width; // :16;
		static short height; // :16;
	}

	final int T_FB_COPY_SIZE = 12;

	/*
	 * T_FB_SFILL Description: Solid Fill command. Tells the client to fill the
	 * area described by x,y,w,h with the pixel value pixel. Size: 12 bytes
	 * Data: NONE //
	 */
	// public class thinc_fbSFill {
	// short x; //:16;
	// short y; //:16;
	// short width; //:16;
	// short height; //:16;
	// int pixel; //:32;
	// }
	// final int T_FB_SFILL_SIZE = 12;
	//
	/* FIXME: Change name from "pixel" to "color" */
	public class thinc_fbSFill {
		int pixel; // :32;
		short numrects; // :16;
		char pad[] = new char[2]; // char(2)
		/*
		 * followed by numrects thinc_rectRegion structs describing each of the
		 * regions to be filled with this colr
		 */
	}

	final int T_FB_SFILL_SIZE = 6;

	/*
	 * T_FB_PFILL Description: Pixmap Fill command: tile pixmap along
	 * rectangles. x,y tells the client where's the origin of the tiling region.
	 * Note that the rectangles may (and most of the time will) describe a
	 * subregion of the tiling region, for example if only the lower left corner
	 * of the region needs to be painted. This information is necessary for
	 * proper alignment of the pixmap, ie. the client shouldn't just paint the
	 * pixmap starting at the origin of each of the rectangles, clipping and
	 * alignment needs to be done. Some implementation notes: -> The pixmap will
	 * probably have a maximum size, above which some other type of update
	 * should be used. -> If the region to be filled is smaller than the pixmap,
	 * another type of message should be used. Size: 10 bytes Data: The pixmap
	 * (width*height*bpp bytes) followed by numrects thinc_rectRegion structures
	 * describing each of the regions that should be filled with this pixmap.
	 */
	public class thinc_fbPFill {
		/* the pixmap's origin */
		static short x; // :16;
		static short y; // :16;
		/* the pixmap size */
		static short width; // :16;
		static short height; // :16;
		/* number of rectangles */
		static short numrects; // :16;
		static char pad[] = new char[2]; // char(2)
		/* followed by the pixmap and numrects thinc_rectRegion's (see below) */
	}

	final int T_FB_PFILL_SIZE = 10;

	final byte T_FB_PFILL_RESIZED = 0x01;
	final byte T_FB_PIXMAP_CACHED = 0x02;
	final byte T_FB_PIXMAP_ADDCACHE = 0x04;

	/*
	 * T_FB_GLYPH Description: Glyph Fill command. Fill the region described by
	 * (x,y) (width, height) using the bitmap as a stipple to fill the region:
	 * If there's a 1 on the bitmap the color specified in the message should be
	 * applied. If there's a 0 no operation should be performed on that pixel.
	 * Size: 12 bytes Data: The bitmap to use as stipple. Bitmap's size is:
	 * ceiling(width/8)*height where 8 is the number of pixels that are packed
	 * on every byte of the bitmap. There's a macro defined in the sample server
	 * and client implementation for this number and for the bitmap's size,
	 * given its width and height.
	 */
	public class thinc_fbGlyph {
		/* the region to fill */
		static short x; // :16;
		static short y; // :16;
		static short width; // :16;
		static short height; // :16;
		/* the color to use */
		static int color; // :32;
		/* number of rectangles. if 0 just blast the glyph onto the fb */
		static short numrects; // :16;
		static char pad[] = new char[2]; // char(2)
		/* followed by a bitmap (width * height)/8 bytes long */
		/* followed by numrects thinc_rectRegion */
	}

	final int T_FB_GLYPH_SIZE = 14;

	/*
	 * T_FB_BILEVEL Description: Bilevel Fill command. Fill the region described
	 * by (x,y) (width, height) using the bitmap as a stipple to fill the
	 * region: If there's a 1 on the bitmap the fg color specified in the
	 * message should be applied. If there's a 0, the bg color specified in the
	 * message should be applied. Size: 16 bytes Data: The bitmap to use as
	 * stipple. Bitmap's size is: ceiling(width/8)*height where 8 is the number
	 * of pixels that are packed on every byte of the bitmap. There's a macro
	 * defined in the sample server and client implementation for this number
	 * and for the bitmap's size, given its width and height.
	 */
	public class thinc_fbBilevel {
		/* the region to fill */
		static short x; // :16;
		static short y; // :16;
		static short width; // :16;
		static short height; // :16;
		/* the colors to use */
		static int fg; // :32;
		static int bg; // :32;
		/* number of rectangles. If 0, just blast the bilevel onto the fb */
		static short numrects; // :16;
		static char pad[] = new char[2]; // char(2)
		/* followed by a bitmap (width * height)/8 bytes long */
		/* followed by numrects thinc_rectRegion */
	}

	final int T_FB_BILEVEL_SIZE = 18;

	/* flags for glyph and bilevel */
	final byte T_FB_BITMAP_CACHED = 0x01; /* 1 */
	final byte T_FB_BITMAP_ADDCACHE = 0x02; /* 1 */
	final byte T_FB_BITMAP_RESIZED = 0x04;

	/** ************** Cursor messages ***************** */

	/*
	 * T_CURSOR_CHANGE Description: The cursor shape needs to be changed to the
	 * shape being sent as data to this message. If the flags contains
	 * T_CURSOR_ARGB then the new cursor is an argb image. Otherwise it's a
	 * bitmap Size: 4 bytes Data: If it's not argb, a bitmap of size size
	 * corresponding to the coursor's source and mask (each of size size/2). If
	 * it's argb, size bytes of pixel data.
	 */
	public class thinc_cursorChange {
		static byte xhot; // :8;
		static byte yhot; // :8;
		static short size; // :16;
	}

	final int T_CURSOR_CHANGE_SIZE = 4;

	/* flags */
	final byte T_CURSOR_ARGB = 0x01;

	/*
	 * T_CURSOR_SHOWHIDE Description: Client should show/hide its current cursor
	 * according to the contents of showhide: T_SHOW, T_HIDE. Cursor Hide is an
	 * implementation dependent operation (it is required). The server may
	 * choose to support it or replace it with an equivalent operation (like
	 * sending a transparent cursor), and the client is free to implement it if
	 * its windowing system allows such an operation, implement it some other
	 * way (such as having a transparent cursor on hand and using it when a Hide
	 * command comes in), or ignoring it. The reasons for this: -> Some
	 * windowing systems (eg. X) don't support a cursor hiding operation. -> The
	 * sample server is able to implement cursor hide as a normal operation or
	 * as sending a transparent cursor. It may decide either way depending on
	 * the knowledge it has of what its clients support. Size: 1 byte Data: None
	 */
	public class thinc_cursorShowHide {
		static byte showhide; // :8;
		static char pad[] = new char[3]; // char(3)
	}

	final int T_SHOW = 1;
	final int T_HIDE = 0;

	final int T_CURSOR_SHOWHIDE_SIZE = 1;

	/*
	 * T_CURSOR_MOVE Description: Client should move the cursor to the specified
	 * position. Size: 4 bytes Data: None
	 */
	public class thinc_cursorMove {
		static short x; // :16;
		static short y; // :16;
	}

	final int T_CURSOR_MOVE_SIZE = 4;

	/*
	 * T_CURSOR_COLOR Description: Change the cursor's fg and bg colors. Only
	 * for non-argb cursors. Size: 8 bytes Data: None
	 */
	public class thinc_cursorColor {
		static int fg; // :32;
		static int bg; // :32;
	}

	final int T_CURSOR_COLOR_SIZE = 8;

	/** ************************** The messages ****************************** */

	final int T_CSTART_SIZE = 3;

	/*
	 * T_CAUTH Description: Has authentication information from the user. The
	 * message itself contains the lenght of the username and password. The
	 * actual strings follow the message. The strings do not end in '\0' (as
	 * normal C strings do) and are not padded Size: 4 bytes Data: Two strings,
	 * representing the username and password. The length of each string is
	 * passed in the actual message
	 */
	public class thinc_cAuth {
		static short user_len; // :16
		static short pass_len; // :16
		/*
		 * followed by username (user_len bytes) and password (pass_len bytes).
		 * the strings do not end in \0
		 */
	}

	final int T_CAUTH_SIZE = 4;

	/*
	 * T_CACK Description: Used by the client to acknowledge certain messages.
	 * It's just 8 bits set to either 1 or 0, representing the clients
	 * acceptance or denial to a particular server message. Size: 1 Data: NONE
	 */
	public class thinc_cAck {
		static byte ack; // :8;
		static char pad[] = new char[3]; // char(3)
	}

	final int T_CACK_SIZE = 1;

	/*
	 * T_EV_MOTION Description: An mouse motion event being reported by the
	 * client. Position reported as x,y coordinates. Size: 4 Data: NONE
	 */
	public class thinc_evMotion {
		static short x; // :16; /* the location of the mouse */
		static short y; // :16;
	}

	final int T_EV_MOTION_SIZE = 4;

	/*
	 * T_EV_BUTTON Description: A button was pressed/released. The 8 bits encode
	 * the information needed: 7 6 - 0 [ Up/Down | Button # ] The button # is
	 * encoded as a number between 0-127. Are there mice with 128 buttons?? :)
	 * Up/Down should use T_UP/T_DOWN (see below) Size: 1 Data: NONE
	 */
	public class thinc_evButton {
		static byte buttonMask; // :8; /* info about button that triggered the
								// event*/
		static char pad[] = new char[3]; // char(3)
	}

	final int T_EV_BUTTON_SIZE = 1;

	final int T_UP = 0;
	final int T_DOWN = 1;

	/*
	 * T_EV_KEYB Description: A key was pressed/released (T_DOWN/T_UP) XXX:
	 * Currently, the key is encoded as an X keysym. Some discussion needs to
	 * happen on this, especially considering the translation from Java codes to
	 * keysym. Size: 5 Data: NONE
	 */
	public class thinc_evKeyb {
		int key; // :32 the code for the key which generated the event */
		byte down; // :8 /* it's pressed (T_DOWN) or released (T_UP) */
		char pad[] = new char[3]; // char(3)
	}

	final int T_EV_KEYB_SIZE = 5;

}
