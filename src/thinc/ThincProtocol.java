/*********************************************
 * Thinc Client
 * Amortya Ray & Divya Arora
 * {ar2566, da2254} @columbia.edu
 * Fall 2006-Fall 2007
 *********************************************/

package thinc;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;
import java.util.zip.Inflater;

import javax.media.CannotRealizeException;
import javax.media.NoPlayerException;

/**
 * ThincProtocol class. This class contains all of the data structures used for
 * client/server communication that comprise the THINC protocol.
 * <p>
 * There are Server->Client messages and Client->Server messages. Each message
 * has a corresponding type that identifies it. Types from s->c may overlap
 * types from c->s.
 */

public class ThincProtocol extends Applet implements ThincMessageTypes,
		ThincMessageStructures, ThincClientUtil, Runnable, ComponentListener {
	// public static Logger logger1 =
	// ThincLogger.getLogger(ThincProtocol.class);
	public static boolean appletflag;
	public static Applet main;
	// static constants
	static final byte[] CLIENT_VERSION = new byte[] { 'T', 'H', 'I', 'N', 'C',
			'_', '0', '.', '2', '\0' };
	static final int CLIENT_SEC_CAPS = 2;
	static final byte[] USER = new byte[] { 't', 'h', 'i', 'n', 'c' };
	// static final byte[] USER = new byte[] {'d','a','2','2','5','4'};
	// static final byte[] PASSWORD = new byte[] {'1','5','m','a','y','8','1'};
	static final byte[] PASSWORD = new byte[] { 'c', 'n', 'i', 'h', 't' };
	static final String THINC_CURSOR = "THINC_CURSOR";
	static final byte IS_COMPRESSED_MASK = 0x01;
	static final byte IS_VIDEO_OKAY = 1;
	static final int MAX_VIDEO_STREAMS = 10;
	static final boolean SOUND = true;
	static final boolean VIDEO = true;
	private static final boolean DEBUG = true;
	int count = 1;

	// for video
	static final int FORMAT_NUM_SUPPORTED = 1;
	static final int FORMAT_FOURCC_YV12 = 0x32315659;
	static final int[] FORMATS_SUPPORTED = { FORMAT_NUM_SUPPORTED };

	// video properties
	public int id; /* id for this video stream */
	public int fmt_id; /*
						 * format. Only send id, which should have been set on
						 * initialization
						 */
	public Short videox; /* position */
	public Short videoy;
	public Short Width; /* dimensions */
	public Short height;
	public Short dst_width; /* for client scaling */
	public Short dst_height;
	public int yuv_yoffset; /* format specific properties */
	public int yuv_uoffset;
	public int yuv_voffset;
	public int yuv_ypitch;
	public int yuv_upitch;
	public int yuv_vpitch;
	public int size; /* display specific info */
	public int timestamp;

	// video data arrays
	int[][] Y;
	int[][] V;
	int[][] U;
	int dataSizeY;
	int dataSizeVU;

	// member variables
	static public SocketChannel sc;
	// static public Selector selector;
	static public ThincMessageHeader sHeader = new ThincMessageHeader();
	public static final ThincSMessage[] serverMessages = new ThincSMessage[150];

	static boolean DO_SSL = false;
	static boolean DO_AUTH = false;
	static boolean APP_SHARE = false;
	ThincSound soundThread;
	ThincVideo videoThread;
	Frame frame;
	static thinc_replyFBInfo fbInfo;
	static thinc_replyCursor cursor;
	static thinc_replyEncoder encoder;
	static thinc_replyFBData fbData;
	static thinc_replyCacheSz cacheSz;
	ThincClientCanvas canvas = new ThincClientCanvas(this);
	Inflater decompressor = new Inflater();
	ByteOrder defaultByteOrder = ByteOrder.BIG_ENDIAN;

	ThincClientHandShake hs = new ThincClientHandShake(this);

	ThincProtocol() {
		// Should not be able to instantiate without arguments
	}

	public void run() {
		try {
			startEventLoop();
		} catch (Exception exp) {
			exp.printStackTrace();
		}
	}

	/**
	 * Default constructor. Requires host and port parameters. Attempts to make
	 * connection to host and port.
	 * 
	 * @param host
	 *            a <code>String</code> representing the host
	 * @param port
	 *            an <code>int</code> representing the port number
	 * @throws ThincException
	 *             if an error occurs upon opening the <code>
	 * SocketChannel</code>
	 *             connection.
	 */
	ThincProtocol(String appName, String host, int port, boolean app, Applet mn)
			throws ThincException {
		appletflag = app;
		main = mn;

		sc = createConnection(host, port);
		// selector = openSelector(sc);
		try {
			initMessages();
			initThincClientGraphics(appName);
			if (SOUND) {
				System.out.println("Init Sound");
				soundThread = new ThincSound(host, port);
				soundThread.start();

				// System.out.println("Sound started!");
				// Thread.sleep(100000);
			}

			hs.doHandShake(host, port);
			videoThread = new ThincVideo(host, port, this);
			sc.configureBlocking(false);
		} catch (ThincException e) {
			System.out.println("Sending bad ack.");
			sendBadAck();
			throw e;
		} catch (Exception e) {
			throw new ThincException("Error occurred while setting "
					+ "SocketChannel to non-blocking.", e);
		}
		ThincProtocol.debug("Successfully initialized ThincClientCanvas.");
	}

	private void initThincClientGraphics(String appName) {
		// logger1.debug("Entering ThincClientGraphics withing Protocol");
		// create canvas.\
		// canvas = new ThincClientCanvas(this);

		// create the frame
		// frame = new JFrame(appName);
		if (appletflag == false) {
			frame = new Frame(appName);

			// set canvas to frame
			frame.setTitle("ThincClient");
			frame.add(canvas, BorderLayout.CENTER);
			// frame.getContentPane().add(canvas, BorderLayout.CENTER);
			ThincProtocol.debug("Canvas added to Frame.");

			// add a window-closing facility
			// :TODO: need to change the WindowAdapter so that it closes the
			// SocketChannel as well.
			frame.addComponentListener(this);
			frame.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					// e.getWindow().dispose();
					close();
					System.exit(0);
				}
			}

			);
			// frame.setResizable(true);
		} else {
			main.add(canvas, BorderLayout.CENTER);
			// frame.getContentPane().add(canvas, BorderLayout.CENTER);

		}
		// try {
		// serverMessages[ThincSMessageInitFrameBuffer.MESSAGE_TYPE].processServerMessage(sc);
		// } catch (ThincException e) {
		// }

		// logger1.debug("Exiting ThincClientGraphics withing Protocol");
	}

	/**
	 * Initializes local <code>HashMap</code> of reusable message objects.
	 */

	private void initMessages() {
		// logger1.debug("Entering initMessages withing Protocol");
		// ------------------------- Server messages -------------------------//
		// framebuffer init
		serverMessages[ThincSMessageInitFrameBuffer.MESSAGE_TYPE] = new ThincSMessageInitFrameBuffer(
				this, appletflag);
		// raw update
		serverMessages[ThincSMessageRawUpdate.MESSAGE_TYPE] = new ThincSMessageRawUpdate(
				this);
		// serverMessages[ThincSMessageRawUpdate.MESSAGE_TYPE] = new
		// ThincSMessageInitFrameBuffer(this);

		// copy rect
		serverMessages[ThincSMessageCopy.MESSAGE_TYPE] = new ThincSMessageCopy(
				this);
		// fill rect
		serverMessages[ThincSMessageFillSolid.MESSAGE_TYPE] = new ThincSMessageFillSolid(
				this);
		// serverMessages[ThincSMessageFillSolid.MESSAGE_TYPE] = new
		// ThincSMessageInitFrameBuffer(this);
		// fill pixmap
		serverMessages[ThincSMessageFillPixMap.MESSAGE_TYPE] = new ThincSMessageFillPixMap(
				this);
		// fill glyph
		serverMessages[ThincSMessageFillGlyph.MESSAGE_TYPE] = new ThincSMessageFillGlyph(
				this);
		// fill bilevel
		serverMessages[ThincSMessageFillBilevel.MESSAGE_TYPE] = new ThincSMessageFillBilevel(
				this);
		// cursor change
		serverMessages[ThincSMessageCursorChange.MESSAGE_TYPE] = new ThincSMessageCursorChange(
				this);
		// cursor move
		serverMessages[ThincSMessageCursorMove.MESSAGE_TYPE] = new ThincSMessageCursorMove(
				this);
		// cursor showHide
		serverMessages[ThincSMessageCursorShowHide.MESSAGE_TYPE] = new ThincSMessageCursorShowHide(
				this);
		// cursor color
		serverMessages[ThincSMessageCursorColor.MESSAGE_TYPE] = new ThincSMessageCursorColor(
				this);
		// logger1.debug("Exiting initMessages withing Protocol");
	}

	/**
	 * Main event loop for handling THINC server messages.
	 * 
	 * @throws ThincException
	 *             if an error occurs reading messages from the server
	 * @throws IOException
	 * @throws CannotRealizeException
	 * @throws NoPlayerException
	 */

	void startEventLoop() throws ThincException, NoPlayerException,
			CannotRealizeException, IOException {
		// logger1.debug("Entering startEventLoop withing Protocol");
		ThincProtocol.debug("Starting main event loop.");

		// while(true){
		// hs.hsFBDataRequest();
		// }
		// check for messages from the server
		ByteBuffer bb = allocateHeaderByteBuffer(4);
		ThincSMessage sMessage;

		while (true) {
			int a = 0;
			try {
				// System.out.print("\nNEXT!!! ");
				while (a++ < 10)
					readBytes(sc, bb);
			} catch (IOException e) {
				throw new ThincException(
						"An error occurred while GETTING HEADER!!!", e);
			}

			// System.out.println("IN LOOP FROM SERVER:"+4);
			// for(int i=0; i<4; ++i){
			// System.out.print(bb.get(i)+" ");
			// }
			// System.out.println();
			bb.flip();
			ByteBuffer t = bb.duplicate();
			byte[] d = new byte[4];
			for (int k = 0; k < 4; k++) {
				d[k] = t.get();
			}
			sHeader.type = bb.get();
			// logger1.info("sHeader.type = " +sHeader.type);
			System.out.println("sHeader.type = " + sHeader.type);
			sHeader.flags = bb.get();
			// logger1.info("sHeader.flags = " +sHeader.flags);
			System.out.println("sHeader.flags = " + sHeader.flags);
			sHeader.length = bb.getShort();
			// logger1.info("sHeader.length = " +sHeader.length);
			System.out.println("sHeader.length = " + sHeader.length);

			if (sHeader.type >= 100 && sHeader.type <= 115) {
				System.out.println("Video initialisation msg");
				switch (sHeader.type) {
				case 110: {
					videoThread.startVideo(sc); /* thinc_vidStart */
					break;
				}
				case 111: {
					videoThread.nextVideo(); /* thinc_vidNext */
					break;
				}

				case 112: {
					videoThread.endVideo(); /* thinc_vidEnd */
					break;
				}

				case 113: {
					videoThread.moveVideo(); /* thinc_vidMove */
					break;
				}
				case 114: {
					videoThread.scaleVideo(); /* thinc_vidScale */
					break;
				}
				case 115: {
					videoThread.resizeVideo(); /* thinc_vidResize */
					break;
				}
				}
			}

			if ((sHeader.type > 11 && sHeader.type < 18)
					|| (sHeader.type > 39 && sHeader.type < 44)) {
				serverMessages[sHeader.type].processServerMessage(sc);
			}
			// sendUpdaterequest();
			// sMessage = getThincServerMessage(sHeader.type);
			// sMessage.processServerMessage(sc);
			bb.clear();

			// logger1.debug("Exiting startEventLoop withing Protocol");
		}
	}

	private void sendClientCloseConn() throws ThincException {
		// logger1.debug("Eentering sendClientCloseConn within Protocol");
		ByteBuffer bb = allocateHeaderByteBuffer(4);
		bb.put((byte) T_CLIENT_LAST);
		bb.put((byte) 0);
		bb.putShort((short) 0);
		bb.flip();
		try {
			sendBytes(getSocketChannel(), bb);
			// ThincProtocol.debug(getStringFromByteBuffer(bb));
		} catch (IOException e) {
			throw new ThincException(
					"An error occurred while sending version.", e);
		}
		// logger1.debug("Eentering sendClientCloseConn within Protocol");
	}

	private String getStringFromBytes(byte[] b) {
		// logger1.debug("Eentering getStringFromBytes within Protocol");
		StringBuffer strBuf = new StringBuffer();
		for (int i = 0; i < b.length; i++) {
			strBuf.append(b[i]);
		}
		// logger1.debug("Exiting getStringFromBytes within Protocol");
		return strBuf.toString();
	}

	private String getStringFromByteBuffer(ByteBuffer bb) {
		// logger1.debug("Eentering getStringFromByteBuffer within Protocol");
		StringBuffer strBuf = new StringBuffer();
		for (int i = 0; i < bb.limit(); i++) {
			strBuf.append(bb.get(i));
		}
		// logger1.debug("Exiting getStringFromByteBuffer within Protocol");
		return strBuf.toString();
	}

	/**
	 * Private utility method for creating a connection.
	 * 
	 * @param host
	 *            <code>String</code> object representing the hostname
	 * @param port
	 *            primitive int representing the port number
	 * @return a fully-initialized <code>SocketChannel</code> object.
	 * @throws ThincException
	 *             if an error occurred during connection.
	 */
	private SocketChannel createConnection(String host, int port)
			throws ThincException {
		// logger1.debug("Eentering createConnection within Protocol");
		System.out.println("Attempting to connect to host " + host + ", port "
				+ port + "...");
		SocketChannel sc;
		try {
			sc = SocketChannel.open();
			// set to block until initial handshake is completed.
			sc.configureBlocking(true);
			sc.connect(new InetSocketAddress(host, port));
			sc.finishConnect();
		} catch (IOException e) {
			throw new ThincException("An error occurred while attempting to "
					+ "connect to host " + host + ", port " + port + ".", e);
		}

		if (!sc.isConnected()) {
			throw new ThincException("Could not connect to server with host "
					+ host + ", port " + port + ". Handshaking was not "
					+ "successful.");
		} else {
			System.out.println("Handshake was successful!");
		}

		System.out.println("Connection opened.");
		// logger1.debug("Exiting createConnection within Protocol");
		return sc;
	}

	// private Selector openSelector(SocketChannel sc) throws ThincException {
	// Selector sel = null;
	// try {
	// sc.configureBlocking(false);
	// sel = Selector.open();
	// sc.register(sel, SelectionKey.OP_READ);
	// } catch (IOException e) {
	// throw new ThincException("Error occurred while opening Selector."
	// , e);
	// }
	// return sel;
	// }

	// ----------------------- client-to-server methods
	// -----------------------//
	/**
	 * Sends the current version of the client to the server in default byte
	 * order format. Note that before the handshake, all data are sent and read
	 * using big-endian format by default. The total number of bytes is 1 (for
	 * header) + 3 (for version number) = <b>4</b>.
	 * 
	 * @throws ThincException
	 *             if an error occurs while attempting to send message to server
	 */

	/**
	 * Sends an "acknowledge" to the server. This method sends a message of type
	 * "10" and is 6 bytes in size.
	 * 
	 * @throws ThincException
	 *             if an error occurs while attempting to send message to server
	 */
	void sendGoodAck() throws ThincException {
		// logger1.debug("Eentering sendGoodAck within Protocol");
		ByteBuffer bb = allocateHeaderByteBuffer(6);
		// message type
		bb.put((byte) 10);
		// message flags -- garbage for now
		bb.put((byte) 0);
		// message length -- garbage for now
		bb.putShort((short) 0);
		// good ack
		bb.put((byte) 1);
		bb.flip();
		try {
			sendBytes(getSocketChannel(), bb);
		} catch (IOException e) {
			throw new ThincException("An error occurred while sending good "
					+ "ack", e);
		}
		// logger1.debug("Exiting sendGoodAck within Protocol");
	}

	/**
	 * Sends an "no acknowledge" to the server. This method sends a message of
	 * type "10" and is 2 bytes in size.
	 * 
	 * @throws ThincException
	 *             if an error occurs while attempting to send message to server
	 */
	void sendBadAck() throws ThincException {
		// logger1.debug("Entering sendBadAck within Protocol");
		ByteBuffer bb = allocateHeaderByteBuffer(2);
		// message type
		bb.put((byte) 10);
		// message flags -- garbage for now
		bb.put((byte) 0);
		// message length -- garbage for now
		bb.putShort((short) 0);
		// bad ack
		bb.put((byte) 0);
		bb.flip();
		try {
			sendBytes(getSocketChannel(), bb);
		} catch (IOException e) {
			throw new ThincException("An error occurred while sending bad "
					+ "ack", e);
		}
		// logger1.debug("Exiting sendBadAck within Protocol");
	}

	/**
	 * Sends message to server stating whether or not video can be handled. This
	 * method sends a message of type "110" and is 5 bytes in size.
	 * 
	 * @param videoId
	 *            the video stream to which the client is replying.
	 * @throws ThincException
	 *             if an error occurs when sending this message
	 */

	// ----------------------- server-to-client methods
	// -----------------------//
	/**
	 * Reads the current version of the server in default byte order format.
	 * Note that before the handshake, all data are sent and read using
	 * big-endian format by default.
	 * 
	 * @throws ThincException
	 *             if an error occurs while attempting to send message to server
	 * @see <code>{@link ThincSMessageStart}</code>
	 */
	private void readServerVersionAndByteOrder() throws ThincException {
		// logger1.debug("Entering ReadServerversionandbyteorder");
		ThincSMessage message = new ThincSMessageStart(this);
		message.processServerMessage(sc);
		// logger1.debug("Exiting ReadServerversionandbyteorder within
		// Protocol");
	}

	/**
	 * Retrieves a <code>ThincSMessage</code> object kept in a local
	 * <code>HashMap</code>.
	 * 
	 * @param messageType
	 *            a primitive integer representing the message type
	 * @return a matching <code>ThincSMessage</code>. Can return null
	 * @throws ThincException
	 *             if the messageType cannot be handled.
	 */
	private ThincSMessage getThincServerMessage(short messageType)
			throws ThincException {// logger1.debug("Entering
		// getThincServerMessage within Protocol");

		if (messageType >= serverMessages.length
				|| null == serverMessages[messageType]) {
			throw new ThincException("Server message type " + messageType + " "
					+ "is not recognized. Data may have been misread from the "
					+ "main SocketChannel or support for a new message type "
					+ "may need to be added to this client.");
		}
		// logger1.debug("Exiting getThincServerMessage within Protocol");
		return serverMessages[messageType];
	}

	// ----------------------- general utility methods -----------------------//
	static int sendBytes(SocketChannel sc, ByteBuffer bb) throws IOException {
		// logger1.debug("Entering sendBytes within Protocol");
		int bytesSent = 0;
		while (bb.hasRemaining()) {
			bytesSent += sc.write(bb);
		}
		// System.out.println("TO SERVER:"+bytesSent);
		// for(int i=0; i<bytesSent; ++i){
		// System.out.print(bb.get(i)+" ");
		// }
		// System.out.println();
		// logger1.debug("Exiting sendBytes within Protocol");
		return bytesSent;
	}

	static int readBytes(SocketChannel sc, ByteBuffer bb) throws IOException {
		// logger1.debug("Entering readBytes within Protocol");
		System.out.println("Entering readBytes within Protocol");
		int bytesRead = 0;
		while (bb.hasRemaining()) {
			try {
				int t = sc.read(bb);
				bytesRead += t;
			} catch (java.nio.channels.ClosedChannelException e) {
				// do nothing
			}
		}
		return bytesRead;
	}

	/**
	 * Returns <code>SocketChannel</code> created upon instantiation of
	 * <code>ThincProtocol</code> object.
	 * 
	 * @return a <code>SocketChannel</code> object, never null
	 */
	SocketChannel getSocketChannel() {
		return sc;
	}

	/**
	 * Cleans up resources by closing the <code>SocketChannel</code> and the
	 * main <code>Frame</code>.
	 */
	void close() {
		// logger1.debug("Entering close() within Protocol");
		if (null != frame) {
			System.out.println("Closing Window.");
			frame.dispose();
		}

		if (decompressor.getRemaining() > 0) {
			int read = -1;
			System.out.println("There are " + decompressor.getRemaining()
					+ " bytes left to decompress.");
			try {
				read = readBytes(sc, allocateByteBuffer(decompressor
						.getRemaining()));
			} catch (IOException e) {
				System.out
						.println("Could not read remaining "
								+ decompressor.getRemaining()
								+ " bytes to decompress.");
			}
			System.out.println("Read " + read + " of them");
		}
		decompressor.end();

		if (!(null != sc && sc.isOpen())) {
			return;
		}

		try {
			System.out.println("Closing SocketChannel.");
			sc.close();
		} catch (IOException e) {
			System.out.println("Error closing SocketChannel. Reason: "
					+ e.toString());
			System.out.println("Nulling SocketChannel..");
			sc = null;
		}
		// logger1.debug("Exiting close within Protocol");
	}

	/**
	 * Factory method for returning a <code>ByteBuffer</code> object of the
	 * proper byte order as determined at handshake time.
	 * 
	 * @param capacity
	 *            an <code>int</code> representing number of bytes to allocate
	 *            for the <code>ByteBuffer</code>
	 * @return a non-null <code>ByteBuffer</code> object
	 */
	ByteBuffer allocateByteBuffer(int capacity) {
		// logger1.debug("Entering and exiting allocateByteBuffer within
		// Protocol");
		return ByteBuffer.allocate(capacity).order(defaultByteOrder);
	}

	/**
	 * Factory method for wrapping byte arrays using proper byte ordering.
	 * 
	 * @param array
	 *            array to be wrapped
	 * @return a properly ordered <code>ByteBuffer</code>
	 */
	ByteBuffer wrapByteArray(byte[] array) {
		return ByteBuffer.wrap(array).order(defaultByteOrder);
	}

	/**
	 * Factory method for wrapping byte arrays using proper byte ordering.
	 * 
	 * @param array
	 *            array to be wrapped
	 * @param offset
	 *            offset to start wrapping at
	 * @param length
	 *            length of byte array to wrap
	 * @return a properly ordered <code>ByteBuffer</code>
	 */
	ByteBuffer wrapByteArray(byte[] array, int offset, int length) {
		return ByteBuffer.wrap(array, offset, length).order(defaultByteOrder);
	}

	/**
	 * Similar to <code>{@link #allocateByteBuffer}</code> but calls <code>
	 * {@link ByteBuffer#allocateDirect}</code>.
	 * This is used primarily by the message classes and defaults to
	 * <code>{@link ByteOrder#BIG_ENDIAN}
	 * </code>.
	 * 
	 * @param capacity
	 *            an <code>int</code> representing number of bytes to allocate
	 *            for the <code>ByteBuffer</code>
	 * @return a non-null <code>ByteBuffer</code> object
	 */
	public ByteBuffer allocateHeaderByteBuffer(int capacity) {
		return ByteBuffer.allocateDirect(capacity).order(ByteOrder.BIG_ENDIAN);
	}

	/**
	 * Simple boolean test to determine if raw data are compressed
	 * 
	 * @return true if the message header indicates that raw data are
	 *         compressed, false if not
	 */
	boolean isRawDataCompressed() {
		return (T_FB_RUPDATE_COMPRESSED & sHeader.flags) == T_FB_RUPDATE_COMPRESSED;
	}

	boolean isRawDataCache() {
		return (T_FB_RUPDATE_CACHED & sHeader.flags) == T_FB_RUPDATE_CACHED;
	}

	boolean isRawDataAddCache() {
		return (T_FB_RUPDATE_ADDCACHE & sHeader.flags) == T_FB_RUPDATE_ADDCACHE;
	}

	boolean isRawDataResized() {
		return (T_FB_RUPDATE_RESIZED & sHeader.flags) == T_FB_RUPDATE_RESIZED;
	}

	boolean isPixMapAddCache() {
		return (T_FB_PIXMAP_ADDCACHE & sHeader.flags) == T_FB_PIXMAP_ADDCACHE;
	}

	boolean isPixMapResized() {
		return (T_FB_PFILL_RESIZED & sHeader.flags) == T_FB_PFILL_RESIZED;
	}

	boolean isPixMapCache() {
		return (T_FB_PIXMAP_CACHED & sHeader.flags) == T_FB_PIXMAP_CACHED;
	}

	boolean isFillGlyphAndBilevelAddCache() {
		return (T_FB_BITMAP_ADDCACHE & sHeader.flags) == T_FB_BITMAP_ADDCACHE;
	}

	boolean isFillGlyphAndBilevelResized() {
		return (T_FB_BITMAP_RESIZED & sHeader.flags) == T_FB_BITMAP_RESIZED;
	}

	boolean isFillGlyphAndBilevelCache() {
		return (T_FB_BITMAP_CACHED & sHeader.flags) == T_FB_BITMAP_CACHED;
	}

	static void debug(String out) {
		if (DEBUG)
			System.out.println(out);
	}

	public void componentMoved(ComponentEvent e) {
	}

	public void componentResized(ComponentEvent e) {
		if (count == 1) {
			count = 0;
			return;
		} else {
			System.out.println("Height: " + frame.getHeight());
			System.out.println("Width: " + frame.getWidth());

			// Now I need to call the method which sends a message to the server
			// with the new
			// frame size.
			try {
				sendResize(frame.getHeight(), frame.getWidth());
			} catch (Exception ec) {
				ec.printStackTrace();
			}
		}
	}

	private void sendResize(int height2, int width2) throws ThincException,
			IOException {
		if (count == 1) {
			count = 0;
			return;
		} else {
			// Short length = 4;
			ByteBuffer bb = allocateHeaderByteBuffer(THINC_HEADER_SIZE /*
																		 * +
																		 * T_CLIENT_REQ_SIZE
																		 */+ 4);
			// bb.put((byte) T_CLIENT_REQ);
			bb.put((byte) 50);
			bb.put((byte) 0);
			bb.putShort((short) 4);
			// bb.putShort((short)50);
			// bb.putShort((short)0);
			bb.putShort((short) width2);
			bb.putShort((short) height2);
			bb.flip();

			try {
				System.out.println("Sending data from withing sendResize");
				sendBytes(sc, bb);
			} catch (IOException e) {
				throw new ThincException("An error occurred while doing "
						+ "sendResize", e);
			}
			bb.clear();
			if (width2 > canvas.frameBufferWidth
					&& height2 > canvas.frameBufferHeight) {
				canvas.frameBufferWidth = (short) width2;
				canvas.frameBufferHeight = (short) height2;

				// Bpp (bits per pixels) converted to Bytes per pixels by /8
				canvas.frameBufferSize = (canvas.frameBufferWidth
						* canvas.frameBufferHeight * (canvas.frameBufferBpp / 8));

				// initialize framebuffer int array
				// frameBufferSize is in bytes, convert to int[] size by /4
				canvas.rawPixels = new int[canvas.frameBufferSize / 4];
			}
		}
		sendUpdaterequest();
	}

	void sendUpdaterequest() throws ThincException, IOException {

		ByteBuffer bb = allocateHeaderByteBuffer(THINC_HEADER_SIZE + 12);
		// bb.put((byte) T_CLIENT_REQ);
		bb.put((byte) 51);
		bb.put((byte) 0);
		bb.putShort((short) 12);
		// bb.putShort((short)50);
		// bb.putShort((short)0);
		bb.putInt((int) 1);
		bb.putShort((short) 0);
		bb.putShort((short) 0);
		bb.putShort(canvas.frameBufferWidth);
		bb.putShort(canvas.frameBufferHeight);
		bb.flip();
		try {
			System.out.println("Sending data from withing sendResize");
			sendBytes(sc, bb);
		} catch (IOException e) {
			throw new ThincException("An error occurred while doing "
					+ "sendResize", e);
		}
		bb.clear();

	}

	public void componentShown(ComponentEvent e) {
	}

	public void componentHidden(ComponentEvent e) {
	}
}
