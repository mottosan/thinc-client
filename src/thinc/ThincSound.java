/*********************************************
 * Thinc Client
 * Amortya Ray & Divya Arora
 * {ar2566, da2254} @columbia.edu
 * Fall 2006-Fall 2007
 *********************************************/

package thinc;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.*;
import javax.sound.sampled.*;
import javax.sound.sampled.spi.*;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

/**
 * Class that contains main method for reading a PCM signed/unsigned data
 * stream. Can be run on a file or a network stream.
 * 
 * Useful URL for sound formats:
 * http://www.spies.com/~franke/SoundApp/formats.html
 */

public class ThincSound extends Thread {

	// static constants
	private static final String USAGE = "Usage: ThincSound [-f <file>]|[-h <host>]";
	// 1 channel is mono, 2 for stereo, etc...
	private static final int CHANNELS = 2;
	// sample rate is how fast samples follow each other, per channel per second
	private static final float SAMPLE_RATE = 44100;
	// number of bits in the shortest sample
	private static final int SAMPLE_SIZE = 16;
	// a frame contains data for all channels at a particular time. for pcm,
	// this is
	// equal to the sample rate. for other encodings, the frame may contain more
	// data.
	// usually the more compression, the slower the frame rate.
	private static final float FRAME_RATE = 44100;
	// frame size is a function of channels and sample size divided by the
	// number of bits in a byte.
	private static final int FRAME_SIZE = (CHANNELS * SAMPLE_SIZE) / 8;
	private static final AudioFormat.Encoding ENCODING = AudioFormat.Encoding.PCM_SIGNED;
	// endianness refers to how the platform swizzled the bits for the sound.
	private static final boolean IS_BIG_ENDIAN = false;

	// default port for networking
	private static final int DEFAULT_PORT = 30000;
	// /////////////////////////////////////////////////////////////////////
	private static final int TC_SND_PORT_INCR = 10000;
	private static final int TC_SND_PLAYSIZE = 256;
	private static final int TC_SND_BUFSIZE = 1 << 16; /* 2^16 */

	/* for version checking */
	private static final byte[] TC_SND_VERSION = new byte[] { 0, 1, 1 };
	final int T_CSTART = 1; /* thinc_cStart */
	private static final boolean DEBUG = true;

	private static final int BUF_SIZE = 4096;
	private static final int BUF_COUNT = 12;
	static public SocketChannel soundc;
	AudioFormat format;
	SourceDataLine.Info playInfo;
	TargetDataLine.Info recInfo;
	int playOffset;
	private String host;
	private int port;
	SourceDataLine sourceLine = null;
	TargetDataLine targetLine = null;
	boolean recBegin = false;
	boolean plyBegin = false;
	byte[][] captureba;
	boolean[] cap_sent;
	ByteBuffer header;

	DataInputStream is;
	OutputStream os;

	ThincSound(String host, int port) throws ThincException {
		this.host = host;
		this.port = port + TC_SND_PORT_INCR;
		captureba = new byte[BUF_COUNT][BUF_SIZE];
		cap_sent = new boolean[BUF_COUNT];
		for (int i = 0; i < BUF_COUNT; ++i) {
			cap_sent[i] = false;
		}
		// /////////////////////////////////////
		header = allocateHeaderByteBuffer(8); // T_SND_OPEN_SIZE
		header.put((byte) 7);
		header.putShort((short) 0);
		header.put((byte) 0);
		header.put((byte) (BUF_SIZE / 1024));
		header.putShort((short) 0);
		header.put((byte) 0);
		header.flip();
		// /////////////////////////////////////

	}

	public void run() {
		ByteBuffer bb = allocateHeaderByteBuffer(4); // THINC_HEADER_SIZE
		// Connect to Sound Deamon

		try {

			// Create Connection
			// soundc = createConnection(host, 30000);
			System.out.println("SND AFTER CONNECTION");

			// Do Hand Shake Protocol for sound
			try {
				tsdHandShake();
			} catch (KeyManagementException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (NoSuchAlgorithmException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			// soundc.configureBlocking(true);
			System.out.println("SND AFTER HANDSHAKE");

			format = new AudioFormat(ENCODING, SAMPLE_RATE, SAMPLE_SIZE,
					CHANNELS, FRAME_SIZE, FRAME_RATE, IS_BIG_ENDIAN);
			playInfo = new DataLine.Info(SourceDataLine.class, format);
			recInfo = new DataLine.Info(TargetDataLine.class, format);
			try {
				System.out.println("Before LINE!");
				sourceLine = (SourceDataLine) AudioSystem.getLine(playInfo);
				targetLine = (TargetDataLine) AudioSystem.getLine(recInfo);

				System.out.println("After LINE!");
			} catch (Exception e) {
				System.out.println("Error occurred while opening audio: "
						+ e.toString());
			}
			// Loop for Play and Close messages
			while (true) {
				// readBytes(soundc, bb);
				for (int i = 0; i < 4; i++) {
					// while(is.readByte()!=0){
					bb.put(is.readByte());
				}
				bb.flip();
				int type = bb.get();
				int flags = bb.get();
				int length = bb.getShort();
				// System.out.print("\nSND NEXT!!! ");
				// System.out.println("type:"+type+" flags:"+flags+"
				// length:"+length);
				switch (type) {
				case 70:
					break;
				case 71:
					processOpen();
					break;
				case 72:
					processPlay();
					break;
				case 73:
					processClose();
					break;
				case 74:
					break;
				case 75:
					break;
				case 76:
					processRecordBegin();
					break;
				case 77:
					processRecordEnd();
					break;
				default:
					throw new ThincException("Message " + type
							+ " is not recognized");
				}
				bb.clear();
			}
		} catch (ThincException e) {
			System.out.println(e);
		} catch (IOException e) {
			System.out.println("An error occurred while GETTING HEADER!!!");
		}

	}

	void processOpen() throws ThincException {
		// Open Connection
		// ThincSound.debug(":Sound Open.");
		ByteBuffer bb = allocateHeaderByteBuffer(5); // T_SND_OPEN_SIZE
		try {
			for (int j = 0; j < 5; j++) {
				bb.put(is.readByte());
			}
			// readBytes(soundc, bb);
		} catch (IOException e) {
			throw new ThincException(
					"An error occurred processing Open Sound Message", e);
		}
		bb.flip();

		int rate = bb.getShort();
		int bits = bb.get();
		int channels = bb.get();
		boolean is_big_endian;
		if (bb.get() == 0)
			is_big_endian = true;
		else
			is_big_endian = false; // T_BIG
		bb.clear();
		// System.out.println("PO:rate:"+(rate)+" bits:"+bits+"
		// channels:"+channels+" isBE:"+is_big_endian);

		// OPEN SOUND DEVICE ; line thing
		if (!recBegin) {
			// format = new AudioFormat(ENCODING, SAMPLE_RATE, bits, channels,
			// (channels * bits) / 8, SAMPLE_RATE, is_big_endian);
			// playInfo = new DataLine.Info(SourceDataLine.class, format);
			try {
				// System.out.println("Before LINE!");
				// sourceLine = (SourceDataLine) AudioSystem.getLine(playInfo);
				// System.out.println("After LINE!");
				sourceLine.open(format);
				sourceLine.start();
			} catch (Exception e) {
				System.out.println("Error occurred while opening audio: "
						+ e.toString());
			}
		}
	}

	void processPlay() throws ThincException {
		// Play Sound
		ThincSound.debug(":Sound Play.");
		ByteBuffer bb = allocateHeaderByteBuffer(9);// T_SND_PLAY_SIZE
		try {
			for (int j = 0; j < 9; j++) {
				bb.put(is.readByte());
			}
			// readBytes(soundc, bb);
		} catch (IOException e) {
			throw new ThincException(
					"An error occurred processing Play Sound Message", e);
		}
		bb.flip();
		int size = bb.getInt();// :32;
		// int timestamp = bb.getInt();// :32;
		// int id = bb.get();// :8;
		bb.clear();

		playAudio(size);
	}

	void playAudio(int size) throws ThincException {
		// Read Sound data from Socket
		byte[] sndba = new byte[size];
		ByteBuffer snddatabb = ByteBuffer.wrap(sndba).order(
				ByteOrder.nativeOrder());

		int numRead = 0, offset = 0;
		try {
			// System.out.print("S:"+size);
			for (int j = 0; j < size; j++) {
				snddatabb.put(is.readByte());
				numRead = numRead + 1;
			}
			// while (snddatabb.hasRemaining()) {
			// numRead += soundc.read(snddatabb);

			// }
			while (offset < numRead) {
				offset += sourceLine.write(sndba, offset, numRead - offset);
			}

			// while (offset < numRead) {
			// offset += sourceLine.write(sndba, offset, numRead-offset);

			// }
			snddatabb.flip();
			offset = 0;
		} catch (IllegalArgumentException e) {
			try {
				System.out.println("Error occured LINE RESTARTED!!");
				sourceLine = (SourceDataLine) AudioSystem.getLine(playInfo);
				sourceLine.open(format);
				sourceLine.start();
				// numRead += sc.read(snddatabb);
				// System.out.println(" nR2:"+numRead);
				// offset += sourceLine.write(sndba, offset, numRead-offset);
			} catch (Exception f) {
				System.out.println("Error occurred while opening audio: "
						+ f.toString());
			}
			// throw new ThincException("An error occurred processing Play Sound
			// Message", e);
		} catch (IOException e) {
			// throw new ThincException("An error occurred processing Play Sound
			// Message", e);
		}
		snddatabb.clear();
	}

	void processClose() throws ThincException {
		// Close Sound
		ThincSound.debug(":Sound Close.");
		ByteBuffer bb = allocateHeaderByteBuffer(1);// T_SND_CLOSE_SIZE
		try {
			bb.put(is.readByte());

			// readBytes(soundc, bb);
		} catch (IOException e) {
			throw new ThincException(
					"An error occurred processing Close Sound Message", e);
		}
		bb.flip();
		int id = bb.get();// :8;

		if (sourceLine != null)
			if (sourceLine.isOpen()) {
				sourceLine.drain();
				sourceLine.stop();
				sourceLine.close();
			}
		bb.clear();
	}

	void processRecordBegin() throws ThincException {
		// Record Begin
		ThincSound.debug(":Sound Record Begin.");
		ByteBuffer bb = allocateHeaderByteBuffer(5); // T_SND_OPEN_SIZE
		try {
			for (int t = 0; t < 5; t++) {
				bb.put(is.readByte());
			}
			// readBytes(soundc, bb);
		} catch (IOException e) {
			throw new ThincException(
					"An error occurred processing Open Sound Message", e);
		}
		bb.flip();

		int rate = bb.getShort();
		int bits = bb.get();
		int channels = bb.get();
		boolean is_big_endian;
		if (bb.get() == 0)
			is_big_endian = true;
		else
			is_big_endian = false; // T_BIG
		bb.clear();

		recBegin = true;
		// System.out.println("RB:rate:"+(rate>>1)+" bits:"+bits+"
		// channels:"+channels+" isBE:"+is_big_endian);

		// initialize format
		// format = new AudioFormat(ENCODING, rate, bits, channels,
		// (channels * bits) / 8, rate, is_big_endian);
		// format = new AudioFormat(ENCODING, SAMPLE_RATE, SAMPLE_SIZE,
		// CHANNELS,
		// FRAME_SIZE, FRAME_RATE, IS_BIG_ENDIAN);
		// recInfo = new DataLine.Info(TargetDataLine.class, format);
		try {
			// System.out.println("Before LINE!");
			// targetLine = (TargetDataLine) AudioSystem.getLine(recInfo);
			System.out.println("Before OPEN!");
			targetLine.open(format);
			System.out.println("After OPEN!");
			targetLine.start();
			System.out.println("After START!");
			new RecordThread().start();
			new SendThread().start();
			// Thread.sleep(1000);
		} catch (Exception e) {
			System.out.println("Error occurred while Capturing Audio: "
					+ e.toString());
		}
	}

	class RecordThread extends Thread {
		public void run() {
			try {
				System.out.println("RT:Writing Target Line");
				int i = 0;
				while (recBegin) {
					// NEW Read data from line
					if (!cap_sent[i]) {
						// targetLine.read(captureba[i], 0, BUF_SIZE);
						for (int j = 0; j < BUF_SIZE; j++) {
							captureba[i][j] = (byte) 'b';
							// System.out.print(captureba[i][j]);
						}
						System.out.println();
						cap_sent[i] = true;
						// System.out.println("read i:"+i);
						i = (i + 1) % BUF_COUNT;
						Thread.sleep(1);
					} // else System.out.println("Record Waiting!");
				}
				System.out.println("RT:Done writing Target Line");
				// AudioSystem.write(new AudioInputStream(targetLine),
				// AudioFileFormat.Type.WAVE, new File("AUDIOFILE_OUT.wav"));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	class SendThread extends Thread {
		public void run() {
			try {
				System.out.println("ST:Sending Data");
				int i = 0;
				ByteBuffer bb = allocateHeaderByteBuffer(BUF_SIZE);
				while (cap_sent[i] | recBegin) {
					// NEW Read data from line
					if (cap_sent[i]) {
						try {
							bb.wrap(captureba[i]);
							for (int j = 0; j < BUF_SIZE; j++)
								System.out.print(bb.get(i));
							System.out.println();
							sendBytes(soundc, header);
							sendBytes(soundc, bb);
							header.flip();
						} catch (IOException e) {
							System.out.println("Error sending");
						}
						cap_sent[i] = false;
						// System.out.println("sent i:"+i);
						i = (i + 1) % BUF_COUNT;
						Thread.sleep(1);
					}// else System.out.println("Send Waiting!");
				}
				System.out.println("ST:Done sending");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	void processRecordEnd() throws ThincException {
		// Record End
		ThincSound.debug(":Sound Record End.");

		// After Record End
		// targetLine.drain();
		if (targetLine.isOpen()) {
			System.out.println("RE:Closing Target Line");
			targetLine.stop();
			targetLine.close();
		}
		recBegin = false;
	}

	public void recordAudio(AudioFormat format, byte[] databa) {
		DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

		try {
			targetLine = (TargetDataLine) AudioSystem.getLine(info);
			new RecordThread().start();
		} catch (Exception e) {
			System.out.println("Error occurred while Capturing Audio: "
					+ e.toString());
		}
	}

	void tsdHandShake() throws ThincException, UnknownHostException,
			IOException, NoSuchAlgorithmException, KeyManagementException {
		SSLSocketFactory factory = null;
		KeyManager[] km = null;
		TrustManager[] tm = { new AllowAllX509TrustManager() };
		SSLContext sslContext = SSLContext.getInstance("SSL");
		sslContext.init(null, tm, new java.security.SecureRandom());

		factory = sslContext.getSocketFactory();
		SSLSocket socket = (SSLSocket) factory.createSocket("128.59.17.200",
				30000);

		socket.startHandshake();

		is = new DataInputStream(new BufferedInputStream(socket
				.getInputStream(), 16384));
		os = socket.getOutputStream();

	}

	public ByteBuffer allocateHeaderByteBuffer(int capacity) {
		return ByteBuffer.allocateDirect(capacity).order(ByteOrder.BIG_ENDIAN);
	}

	static void debug(String out) {
		if (DEBUG)
			System.out.println(out);
	}

	static int sendBytes(SocketChannel sc, ByteBuffer bb) throws IOException {
		int bytesSent = 0;
		while (bb.hasRemaining()) {
			bytesSent += sc.write(bb);
		}
		// System.out.println("SND TO SERVER:"+bytesSent);
		// for(int i=0; i<bytesSent; ++i){
		// System.out.print(bb.get(i)+" ");
		// }
		// System.out.println();
		return bytesSent;
	}

	static int readBytes(SocketChannel sc, ByteBuffer bb) throws IOException {
		int bytesRead = 0;
		while (bb.hasRemaining()) {
			bytesRead += sc.read(bb);
		}
		// System.out.println("SND FROM SERVER:"+bytesRead);
		// for(int i=0; i<bytesRead; ++i){
		// System.out.print(bb.get(i)+" ");
		// }
		// System.out.println();
		return bytesRead;
	}
}
// Continuously read and play chunks of audio
// WORKING METHOD
// int numRead = 0;
// byte[] sndba = new byte[size];
// ByteBuffer snddatabb = ByteBuffer.wrap(sndba).order(ByteOrder.nativeOrder());
// int offset = 0;
// try {
// System.out.print("S:"+size);
// numRead = sc.read(snddatabb);
// System.out.print(" nR:"+numRead);
// while (offset < numRead) {
// offset += sourceLine.write(sndba, offset, numRead-offset);
// System.out.println(" oS:"+offset);
// }
// snddatabb.flip();
// offset = 0;
// } catch (IOException e) {
// throw new ThincException("An error occurred processing Play Sound Message",
// e);
// }
// ////////////////

