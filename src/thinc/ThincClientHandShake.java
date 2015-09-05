/*********************************************
 * Thinc Client
 * Amortya Ray & Divya Arora
 * {ar2566, da2254} @columbia.edu
 * Fall 2006-Fall 2007
 *********************************************/

package thinc;

import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.ByteBuffer;
import java.io.IOException;
import java.awt.Point;

import org.apache.log4j.Logger;

public class ThincClientHandShake implements ThincMessageTypes,
		ThincMessageStructures {
	public static Logger logger = ThincLogger
			.getLogger(ThincClientHandShake.class);
	ThincProtocol proto;
	static final byte[] CLIENT_VERSION = new byte[] { 'T', 'H', 'I', 'N', 'C',
			'_', '0', '.', '2', '\0' };
	static final int CLIENT_SEC_CAPS = 2;
	static final byte[] USER = new byte[] { 't', 'h', 'i', 'n', 'c' };
	// static final byte[] USER = new byte[] {'d','a','2','2','5','4'};
	static final byte[] PASSWORD = new byte[] { 'c', 'n', 'i', 'h', 't' };
	// static final byte[] PASSWORD = new byte[] {'1','5','m','a','y','8','1'};
	private static final int T_VIDEO_SINIT_SIZE = 4;
	private static final int NUM_CLIENT_VIDEO_FMTS = 1;
	public int[] CLIENT_VIDEO_FMTS = new int[] { 842094169, 844715353,
			1498831189 };
	static boolean VIDEO;
	static boolean DO_SSL = false;
	static boolean DO_AUTH = false;
	static boolean APP_SHARE = false;
	public int numServerFormats;
	public int[] ServerFormats;

	/** Creates a new instance of ThincClientHandShake */
	public ThincClientHandShake(ThincProtocol proto) {
		this.proto = proto;
	}

	public void doHandShake(String host, int port) throws ThincException {
		logger.debug("Entering doHandshake");
		// 1. Version Exchange
		hsVersionExchange();

		// 2. Security Caps Exchange
		hsSecurityCapExcahnge();

		// 3. SSL handshake (opt)
		if (DO_SSL)
			hsSSL();

		// 4. Auth Info Exchange (opt)
		if (DO_AUTH)
			hsAuth();

		// 5. Requests

		hsFBInfoRequest();
		hsVideoRequest();
		if (VIDEO) {
			hsVideoFormatNegotiation();
			hsClientFormatInfo();
		}
		hsCursorRequest();
		hsEncoderRequest();
		hsAppShareRequest();
		if (!APP_SHARE)
			hsFBDataRequest();
		// hsCacheRequest();

		// LOTS OF THINGS BEFORE THIS
		// 6. Send Client Done message
		hsClientDone();
		logger.debug("Exiting doHandShake");
	}

	private void hsClientFormatInfo() throws ThincException {

		ThincProtocol.debug(":1C:Sending client formats.");
		ByteBuffer clientfbb = proto.allocateHeaderByteBuffer(THINC_HEADER_SIZE
				+ T_CLIENT_REQ_SIZE + 8);
		clientfbb.put((byte) T_CLIENT_REQ);
		clientfbb.put((byte) 0);
		clientfbb.putShort((short) 8);
		clientfbb.putShort((short) T_REQ_VIDEO_CLIENT_FMTS);
		clientfbb.putShort((short) 0);
		clientfbb.putInt((int) NUM_CLIENT_VIDEO_FMTS);
		clientfbb.putInt((int) 842094169);
		clientfbb.flip();
		try {
			sendBytes(proto.sc, clientfbb);
		} catch (IOException e) {
			throw new ThincException(
					"An error occurred while sending client formats", e);
		}
		clientfbb.clear();

		ByteBuffer serverreplybb = proto.allocateHeaderByteBuffer(8);
		try {
			readBytes(proto.sc, serverreplybb);
		} catch (IOException e) {
			throw new ThincException("An error occurred while read version", e);
		}
		serverreplybb.flip();
		serverreplybb.getInt();

		// Handle Server Reply
		if (serverreplybb.getShort() != T_REPLY_OK) {
			throw new ThincException("Video format not supported by server");
		}
		serverreplybb.getShort();
		serverreplybb.clear();
		/*
		 * //actual data ByteBuffer client1fbb =
		 * proto.allocateHeaderByteBuffer(16);
		 * client1fbb.putInt((int)NUM_CLIENT_VIDEO_FMTS);
		 * client1fbb.putInt((int)842094169); client1fbb.putInt((int)844715353);
		 * client1fbb.putInt((int)1498831189);
		 * 
		 * client1fbb.flip(); try { sendBytes(proto.sc, client1fbb); } catch
		 * (IOException e) { throw new ThincException("An error occurred while
		 * sending client formats",e); } client1fbb.clear(); /* ByteBuffer
		 * serverreply1bb = proto.allocateHeaderByteBuffer(4); try {
		 * readBytes(proto.sc, serverreply1bb); } catch (IOException e) { throw
		 * new ThincException("An error occurred while read version", e); }
		 * serverreply1bb.flip(); serverreply1bb.getInt();
		 * 
		 * //Handle Server Reply if (serverreply1bb.getShort() != T_REPLY_OK){
		 * throw new ThincException("Video format not supported by server"); }
		 * serverreply1bb.getShort(); serverreply1bb.clear();
		 *  /* //5.1.2 Read Server Response ByteBuffer serverreplybb =
		 * proto.allocateHeaderByteBuffer(8); try { readBytes(proto.sc,
		 * serverreplybb); } catch (IOException e) { throw new
		 * ThincException("An error occurred while read version", e); }
		 * serverreplybb.flip(); serverreplybb.getInt();
		 * 
		 * //Handle Server Reply if (serverreplybb.getShort() != T_REPLY_OK){
		 * throw new ThincException("Video format not supported by server"); }
		 * serverreplybb.getShort(); serverreplybb.clear();
		 */
	}

	private void hsVideoFormatNegotiation() throws ThincException {
		System.out.println("Entering Video Format Negotiation");

		ByteBuffer requestbb = proto.allocateHeaderByteBuffer(THINC_HEADER_SIZE
				+ T_CLIENT_REQ_SIZE);
		requestbb.put((byte) T_CLIENT_REQ);
		requestbb.put((byte) 0);
		requestbb.putShort((short) 4);
		requestbb.putShort((short) T_REQ_VIDEO_SERV_FMTS);
		requestbb.putShort((short) 0);
		requestbb.flip();
		try {
			sendBytes(proto.sc, requestbb);
		} catch (IOException e) {
			throw new ThincException(
					"An error occurred while negotiating video formats", e);
		}
		requestbb.clear();

		// 5.1.2 Read Server Response
		ByteBuffer replybb = proto.allocateHeaderByteBuffer(THINC_HEADER_SIZE
				+ T_SERV_REPLY_SIZE + T_VIDEO_SINIT_SIZE);
		// ByteBuffer replybb = proto.allocateHeaderByteBuffer(8);
		try {
			readBytes(proto.sc, replybb);
		} catch (IOException e) {
			throw new ThincException("An error occurred while read version", e);
		}
		replybb.flip();
		System.out.println("From Server" + replybb.get());
		System.out.println("From Server" + replybb.get());
		System.out.println("From Server" + replybb.get());
		System.out.println("From Server" + replybb.get());
		System.out.println("From Server" + replybb.get());
		System.out.println("From Server" + replybb.get());
		System.out.println("From Server" + replybb.get());
		System.out.println("From Server" + replybb.get());

		numServerFormats = replybb.getInt();

		// Get the diff format ids from the Server
		ByteBuffer formatsbb = proto
				.allocateHeaderByteBuffer(4 * numServerFormats);
		try {
			readBytes(proto.sc, formatsbb);
		} catch (IOException e) {
			throw new ThincException("An error occurred while read version", e);
		}
		formatsbb.flip();

		// handle the version
		ServerFormats = new int[numServerFormats];
		for (int i = 0; i < numServerFormats; i++) {
			ServerFormats[i] = formatsbb.getInt();
			if (ServerFormats[i] != 842094169 && ServerFormats[i] != 844715353
					&& ServerFormats[i] != 1498831189) {
				// :TODO: better exception here? perhaps a ThincServerException?
				throw new ThincException("Incompatible Format found");
			}
		}
		formatsbb.clear();
		logger.debug("Exiting FormatExhange");

	}

	private void hsVideoRequest() throws ThincException {
		System.out.println("entering video request");
		// ByteBuffer bb =
		// proto.allocateHeaderByteBuffer(CLIENT_VERSION.length);
		ByteBuffer requestbb = proto.allocateHeaderByteBuffer(THINC_HEADER_SIZE
				+ T_CLIENT_REQ_SIZE);
		requestbb.put((byte) T_CLIENT_REQ);
		requestbb.put((byte) 0);
		requestbb.putShort((short) 4);
		requestbb.putShort((short) T_REQ_VIDEO);
		requestbb.putShort((short) 0);
		requestbb.flip();
		try {
			sendBytes(proto.sc, requestbb);
		} catch (IOException e) {
			throw new ThincException(
					"An error occurred while sending video request", e);
		}
		requestbb.clear();

		// 5.1.2 Read Server Response
		// ByteBuffer replybb =
		// proto.allocateHeaderByteBuffer(THINC_HEADER_SIZE);
		// +T_SERV_REPLY_SIZE + T_REPLY_OK_SIZE);
		ByteBuffer replybb = proto.allocateHeaderByteBuffer(8);
		try {
			readBytes(proto.sc, replybb);
		} catch (IOException e) {
			throw new ThincException("An error occurred while read version", e);
		}
		replybb.flip();
		replybb.getInt();

		// Handle Server Reply
		if (replybb.getShort() != T_REPLY_OK) {
			throw new ThincException("Video not supported by server");
		}
		VIDEO = true;
		replybb.getShort();
		replybb.clear();
		logger.debug("Exiting hsVideo");
	}

	private void hsVersionExchange() throws ThincException {
		// 1.1 Read Server Version
		logger.debug("Entering VersionExhange");
		ThincProtocol.debug(":1S:Reading server version.");
		ByteBuffer bb = proto.allocateHeaderByteBuffer(CLIENT_VERSION.length);
		try {
			readBytes(proto.sc, bb);
		} catch (IOException e) {
			throw new ThincException("An error occurred while read version", e);
		}
		bb.flip();
		byte[] version = new byte[10];
		bb.get(version);

		// handle the version
		for (int i = 0; i < CLIENT_VERSION.length; i++) {
			if (CLIENT_VERSION[i] != version[i]) {
				// :TODO: better exception here? perhaps a ThincServerException?
				throw new ThincException("Incompatible version found. Version "
						+ "required by client: " + ", version of server: ");
			}
		}
		bb.clear();

		// 1.2 Send Client Version
		ThincProtocol.debug(":1C:Sending client version.");
		bb.put(CLIENT_VERSION);
		bb.flip();
		try {
			sendBytes(proto.sc, bb);
		} catch (IOException e) {
			throw new ThincException(
					"An error occurred while sending version.", e);
		}
		logger.debug("Exiting VersionExhange");
	}

	private void hsSecurityCapExcahnge() throws ThincException {
		logger.debug("Entering CapExhange");
		// 2.1 Read Server Caps
		ThincProtocol.debug(":2S:Reading server caps.");
		ByteBuffer bb = proto.allocateHeaderByteBuffer(THINC_HEADER_SIZE
				+ T_SERV_SEC_CAPS_SIZE);
		try {
			readBytes(proto.sc, bb);
		} catch (IOException e) {
			throw new ThincException("An error occurred while read version", e);
		}
		bb.flip();

		// Evaluate Server Caps
		int SERVER_SEC_CAPS = bb.getInt(4);
		int SESSION_SEC_CAPS = (SERVER_SEC_CAPS & CLIENT_SEC_CAPS);

		// 2.2 Send Client Caps
		ThincProtocol.debug(":2C:Sending client caps.");
		bb.put((byte) T_CLIENT_SEC_CAPS);
		bb.put((byte) 0);
		bb.putShort((short) 4);
		bb.putInt(SESSION_SEC_CAPS);
		bb.flip();
		try {
			sendBytes(proto.sc, bb);
		} catch (IOException e) {
			throw new ThincException(
					"An error occurred while sending version.", e);
		}
		bb.clear();

		// 2.3 Read Session Caps
		ThincProtocol.debug(":2SS:Reading session caps.");
		try {
			readBytes(proto.sc, bb);
		} catch (IOException e) {
			throw new ThincException("An error occurred while read version", e);
		}
		bb.flip();

		SERVER_SEC_CAPS = bb.getInt(4);
		// if ((SESSION_SEC_CAPS & T_CAPS_SEC_ENC) > 0) DO_SSL = true;
		if ((SESSION_SEC_CAPS & T_CAPS_SEC_AUTH) > 0)
			DO_AUTH = true;

		bb.clear();
		logger.debug("Exiting CapExhange");
	}

	private void hsSSL() throws ThincException {
		ThincProtocol.debug(":3C:Sending Client SSL Info.");
		ThincProtocol.debug(":3S:Reading Server SSL Info.");
	}

	private void hsAuth() throws ThincException {
		logger.debug("Entering hsAuth");
		// 4.1 Send Client Auth Info
		ThincProtocol.debug(":4C:Sending Client User Info.");
		ByteBuffer clientAuthbb = proto
				.allocateHeaderByteBuffer(THINC_HEADER_SIZE + T_CAUTH_SIZE
						+ USER.length + PASSWORD.length);
		clientAuthbb.put((byte) T_CLIENT_AUTH);
		clientAuthbb.put((byte) 0);
		clientAuthbb.putShort((short) 4);

		clientAuthbb.putShort((short) 5);
		clientAuthbb.putShort((short) 5);
		clientAuthbb.put(USER);
		clientAuthbb.put(PASSWORD);
		// System.out.println("username: "+USER);
		// System.out.println("password: "+PASSWORD);
		clientAuthbb.flip();
		try {
			sendBytes(proto.sc, clientAuthbb);
		} catch (IOException e) {
			throw new ThincException(
					"An error occurred while sending version.", e);
		}
		clientAuthbb.clear();

		// 4.2 Read Server Auth Reply
		ThincProtocol.debug(":4S:Reading Server User Info.");
		ByteBuffer serverAuthbb = proto
				.allocateHeaderByteBuffer(THINC_HEADER_SIZE);
		try {
			readBytes(proto.sc, serverAuthbb);
		} catch (IOException e) {
			throw new ThincException("An error occurred while read version", e);
		}
		serverAuthbb.flip();

		// Handle Server Reply
		if (serverAuthbb.get() != T_SERV_OK) {
			throw new ThincException("Username or Password is invalid.");
		}
		serverAuthbb.clear();
		logger.debug("Exiting hsAuth");
	}

	private void hsFBInfoRequest() throws ThincException {
		logger.debug("Entering hsFBInfoRequest");
		// 5.1.1 Send Server FBInfo Request
		ThincProtocol.debug(":5(1):FB Info.");
		ByteBuffer requestbb = proto.allocateHeaderByteBuffer(THINC_HEADER_SIZE
				+ T_CLIENT_REQ_SIZE);
		requestbb.put((byte) T_CLIENT_REQ);
		requestbb.put((byte) 0);
		requestbb.putShort((short) 4);
		requestbb.putShort((short) T_REQ_FBINFO);
		requestbb.putShort((short) 0);
		requestbb.flip();
		try {
			sendBytes(proto.sc, requestbb);
		} catch (IOException e) {
			throw new ThincException(
					"An error occurred while sending version.", e);
		}
		requestbb.clear();

		// 5.1.2 Read Server Response
		ByteBuffer replybb = proto.allocateHeaderByteBuffer(THINC_HEADER_SIZE
				+ T_SERV_REPLY_SIZE + T_REPLY_FBINFO_SIZE);
		try {
			readBytes(proto.sc, replybb);
		} catch (IOException e) {
			throw new ThincException("An error occurred while read version", e);
		}

		// 5.1.3 Allot fbInfo reply
		replybb.flip();
		replybb.getInt(); // THINC header
		replybb.getInt(); // Request header
		replybb.getShort(); // byteOrderMbz 1:(T_BIG | T_LITTLE) 15:(Not Used)
		proto.canvas.frameBufferDepth = replybb.get();
		proto.canvas.frameBufferBpp = replybb.get();
		proto.canvas.frameBufferWidth = replybb.getShort();
		proto.canvas.frameBufferHeight = replybb.getShort();

		// Bpp (bits per pixels) converted to Bytes per pixels by /8
		proto.canvas.frameBufferSize = (proto.canvas.frameBufferWidth
				* proto.canvas.frameBufferHeight * (proto.canvas.frameBufferBpp / 8));

		// initialize framebuffer int array
		// frameBufferSize is in bytes, convert to int[] size by /4
		proto.canvas.rawPixels = new int[proto.canvas.frameBufferSize / 4];

		replybb.clear();
		logger.debug("Exiting hsFBInfoRequest");
	}

	private void hsCursorRequest() throws ThincException {
		logger.debug("Entering hsFBCursorRequest");
		// 5.2.1 Send Server Cursor Request
		ThincProtocol.debug(":5(2):Cursor Info.");
		ByteBuffer requestbb = proto.allocateHeaderByteBuffer(THINC_HEADER_SIZE
				+ T_CLIENT_REQ_SIZE);
		requestbb.put((byte) T_CLIENT_REQ);
		requestbb.put((byte) 0);
		requestbb.putShort((short) 4);
		requestbb.putShort((short) T_REQ_CURSOR);
		requestbb.putShort((short) 0);
		requestbb.flip();
		try {
			sendBytes(proto.sc, requestbb);
		} catch (IOException e) {
			throw new ThincException(
					"An error occurred while sending version.", e);
		}
		requestbb.clear();

		// 5.2.2 Read Server Response
		ByteBuffer replybb = proto.allocateHeaderByteBuffer(THINC_HEADER_SIZE
				+ T_SERV_REPLY_SIZE + T_REPLY_CURSOR_SIZE);
		try {
			readBytes(proto.sc, replybb);
		} catch (IOException e) {
			throw new ThincException("An error occurred while read version", e);
		}

		// 5.2.3 Allot cursor reply
		replybb.flip();
		replybb.getInt(); // THINC header
		replybb.getInt(); // Request header
		short flags = replybb.getShort();
		proto.canvas.cursorSize = replybb.getShort();
		proto.canvas.cursorWidth = replybb.get();
		proto.canvas.cursorHeight = replybb.get();
		short cursorXHot = replybb.get();
		short cursorYHot = replybb.get();
		short cursorX = replybb.getShort();
		short cursorY = replybb.getShort();
		proto.canvas.cursorFg = replybb.getInt();
		proto.canvas.cursorBg = replybb.getInt();
		replybb.clear();

		// cursor hotspot
		proto.canvas.cursorHotSpot = new Point(cursorXHot, cursorYHot);

		if ((flags & T_CURSOR_ARGB) > 0)
			proto.canvas.IS_CURSOR_ARGB = true;
		else
			proto.canvas.IS_CURSOR_ARGB = false;

		// System.out.println(proto.canvas.cursorSize);
		// System.out.println("CURflags:"+flags+"
		// IS_CURSOR_ARGB"+proto.canvas.IS_CURSOR_ARGB);

		proto.canvas.cursorSourceBitMap = proto
				.allocateHeaderByteBuffer(proto.canvas.cursorWidth
						* proto.canvas.cursorHeight * 4);
		proto.canvas.cursorMaskBitMap = proto
				.allocateHeaderByteBuffer(proto.canvas.cursorWidth
						* proto.canvas.cursorHeight * 4);
		proto.canvas.cursorSourceBitMap.limit(proto.canvas.cursorSize / 2);
		proto.canvas.cursorMaskBitMap.limit(proto.canvas.cursorSize / 2);
		try {
			readBytes(proto.sc, proto.canvas.cursorSourceBitMap);
			readBytes(proto.sc, proto.canvas.cursorMaskBitMap);
		} catch (IOException e) {
			throw new ThincException("An error occurred while read version", e);
		}
		proto.canvas.cursorSourceBitMap.flip();
		proto.canvas.cursorMaskBitMap.flip();
		logger.debug("Exiting hsFBCursorRequest");
	}

	private void hsEncoderRequest() throws ThincException {
		logger.debug("Entering hsEncoderRequest");
		// 5.3.1 Send Server Encoder Request
		ThincProtocol.debug(":5(3):Encoder Info.");
		ByteBuffer requestbb = proto.allocateHeaderByteBuffer(THINC_HEADER_SIZE
				+ T_CLIENT_REQ_SIZE);
		requestbb.put((byte) T_CLIENT_REQ);
		requestbb.put((byte) 0);
		requestbb.putShort((short) 4);
		requestbb.putShort((short) T_REQ_ENCODER);
		requestbb.putShort((short) 0);
		requestbb.flip();
		try {
			sendBytes(proto.sc, requestbb);
		} catch (IOException e) {
			throw new ThincException(
					"An error occurred while sending version.", e);
		}
		requestbb.clear();

		// 5.3.2 Read Server Response
		ByteBuffer replybb = proto.allocateHeaderByteBuffer(THINC_HEADER_SIZE
				+ T_SERV_REPLY_SIZE + T_REPLY_ENCODER_SIZE);
		try {
			readBytes(proto.sc, replybb);
		} catch (IOException e) {
			throw new ThincException("An error occurred while read version", e);
		}

		// 5.3.3 Allot encode reply
		replybb.flip();
		replybb.getInt();
		proto.encoder.reply_head = replybb.getInt();
		proto.encoder.whichReserved = replybb.getInt();
		replybb.clear();
		logger.debug("Exiting hsFBEncoderRequest");
	}

	private void hsAppShareRequest() throws ThincException {
		logger.debug("Entering hsAppShareRequest");
		// 5.4.1 Send Server App Share Request
		ThincProtocol.debug(":5(4):App Share Info.");
		ByteBuffer requestbb = proto.allocateHeaderByteBuffer(THINC_HEADER_SIZE
				+ T_CLIENT_REQ_SIZE);
		requestbb.put((byte) T_CLIENT_REQ);
		requestbb.put((byte) 0);
		requestbb.putShort((short) 4);
		requestbb.putShort((short) T_REQ_APP_SHARING);
		requestbb.putShort((short) 0);
		requestbb.flip();
		try {
			sendBytes(proto.sc, requestbb);
		} catch (IOException e) {
			throw new ThincException(
					"An error occurred while sending version.", e);
		}
		requestbb.clear();

		// 5.4.2 Read Server Response
		ByteBuffer replybb = proto.allocateHeaderByteBuffer(THINC_HEADER_SIZE
				+ T_SERV_REPLY_SIZE + T_REQ_APP_SHARING_SIZE);
		try {
			readBytes(proto.sc, replybb);
		} catch (IOException e) {
			throw new ThincException("An error occurred while read version", e);
		}

		// 5.3.3 Allot encode reply
		replybb.flip();
		replybb.getInt();
		int appShare = replybb.getShort();
		// if (appShare == 1) APP_SHARE = true;
		System.out.println("app share" + appShare + " " + APP_SHARE);
		replybb.clear();
		logger.debug("Entering hsAppShareRequest");
	}

	public void hsFBDataRequest() throws ThincException {
		logger.debug("Entering hsFBDataRequest");
		// 5.5.1 Send Server FBData Request
		ThincProtocol.debug(":5(5):FBData Info.");
		ByteBuffer requestbb = proto.allocateHeaderByteBuffer(THINC_HEADER_SIZE
				+ T_CLIENT_REQ_SIZE);
		requestbb.put((byte) T_CLIENT_REQ);
		requestbb.put((byte) 0);
		requestbb.putShort((short) 4);
		requestbb.putShort((short) T_REQ_FBDATA);
		requestbb.putShort((short) 0);
		requestbb.flip();
		try {
			sendBytes(proto.sc, requestbb);
		} catch (IOException e) {
			throw new ThincException(
					"An error occurred while sending version.", e);
		}
		requestbb.clear();

		// 5.5.2 Read Server Response
		ByteBuffer replybb = proto.allocateHeaderByteBuffer(THINC_HEADER_SIZE
				+ T_SERV_REPLY_SIZE + T_REPLY_FBDATA_SIZE);
		try {
			readBytes(proto.sc, replybb);
		} catch (IOException e) {
			throw new ThincException("An error occurred while read version", e);
		}

		// 5.5.3 Allot Cache reply
		// fbdata includes data that reads flags for compression and info for
		// decompression
		replybb.flip();
		int t = replybb.getInt();
		System.out.println("t is" + t);
		proto.fbData.reply_head = replybb.getInt();
		System.out.println("reply_head is" + proto.fbData.reply_head);
		proto.fbData.flags = replybb.getInt();
		System.out.println("flag is" + proto.fbData.flags);
		System.out.println("IsCompressed:"
				+ (proto.fbData.flags & T_FB_COMPRESSED));
		try {
			proto.sHeader.flags = (byte) (proto.fbData.flags & T_FB_COMPRESSED);
			proto.serverMessages[ThincSMessageInitFrameBuffer.MESSAGE_TYPE]
					.processServerMessage(proto.sc);
		} catch (ThincException e) {
		}
		replybb.clear();
		logger.debug("Exiting hsFBDataRequest");
	}

	private void hsCacheRequest() throws ThincException {
		// 5.6.1 Send Server FBInfo Request
		ThincProtocol.debug(":5(6):Cache Info.");
		ByteBuffer requestbb = proto.allocateHeaderByteBuffer(THINC_HEADER_SIZE
				+ T_CLIENT_REQ_SIZE);
		requestbb.put((byte) T_CLIENT_REQ);
		requestbb.put((byte) 0);
		requestbb.putShort((short) 4);
		requestbb.putShort((short) T_REQ_CACHESZ);
		requestbb.putShort((short) 0);
		requestbb.flip();
		try {
			sendBytes(proto.sc, requestbb);
		} catch (IOException e) {
			throw new ThincException(
					"An error occurred while sending version.", e);
		}
		requestbb.clear();

		// 5.6.2 Read Server Response
		ByteBuffer replybb = proto.allocateHeaderByteBuffer(THINC_HEADER_SIZE
				+ T_SERV_REPLY_SIZE + T_REPLY_CACHESZ_SIZE);
		try {
			readBytes(proto.sc, replybb);
		} catch (IOException e) {
			throw new ThincException("An error occurred while read version", e);
		}

		// 5.6.3 Allot fbData reply
		replybb.flip();
		replybb.getInt();
		proto.cacheSz.reply_head = replybb.getInt();
		proto.cacheSz.img = replybb.get();
		proto.cacheSz.bit = replybb.get();
		proto.cacheSz.pix = replybb.get();
		proto.cacheSz.reserved = replybb.get();
		replybb.clear();
	}

	private void hsClientDone() throws ThincException {
		logger.debug("Entering hsClientDone");
		ByteBuffer bb = proto.allocateHeaderByteBuffer(4);
		bb.put((byte) T_CLIENT_DONE);
		bb.put((byte) 0);
		bb.putShort((short) 0);
		bb.flip();
		try {
			sendBytes(proto.sc, bb);
			// ThincProtocol.debug(getStringFromByteBuffer(bb));
		} catch (IOException e) {
			throw new ThincException(
					"An error occurred while sending version.", e);
		}
		bb.clear();
		logger.debug("Exiting hsClientdone");
	}

	static int sendBytes(SocketChannel sc, ByteBuffer bb) throws IOException {
		logger.debug("Entering sendBytes");
		int bytesSent = 0;
		while (bb.hasRemaining()) {
			bytesSent += sc.write(bb);
		}
		System.out.println("TO SERVER:" + bytesSent);
		for (int i = 0; i < bytesSent; ++i) {
			System.out.print(bb.get(i) + " ");
		}
		System.out.println();
		logger.debug("Exiting sendbytes");
		return bytesSent;
	}

	static int readBytes(SocketChannel sc, ByteBuffer bb) throws IOException {
		logger.debug("Entering readBytes");
		int bytesRead = 0;
		while (bb.hasRemaining()) {
			bytesRead += sc.read(bb);
		}
		System.out.println("FROM SERVER:" + bytesRead);
		// for(int i=0; i<bytesRead; ++i){
		// System.out.print(bb.get(i)+" ");
		// }
		// System.out.println();
		logger.debug("Exiting readbytes");
		return bytesRead;
	}
}
