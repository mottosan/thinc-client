/*********************************************
 * Thinc Client
 * Amortya Ray & Divya Arora
 * {ar2566, da2254} @columbia.edu
 * Fall 2006-Fall 2007
 *********************************************/

package thinc;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.channels.SocketChannel;

import javax.media.CannotRealizeException;
import javax.media.CaptureDeviceInfo;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.NoPlayerException;
import javax.media.Player;
import javax.media.RealizeCompleteEvent;
import javax.media.format.VideoFormat;
import javax.swing.JFrame;

public class ThincVideo extends JFrame {
	private String host = "blade14.ncl.cs.columbia.edu";
	private int port = 20000;
	static final byte IS_VIDEO_OKAY = 1;
	ByteOrder defaultByteOrder = ByteOrder.BIG_ENDIAN;
	SocketChannel sc;

	// video properties
	public int id; /* id for this video stream */
	public int fmt_id; /*
						 * format. Only send id, which should have been set on
						 * initialization
						 */
	public short videox; /* position */
	public short videoy;
	public short Width; /* dimensions */
	public short height;
	public short dst_width; /* for client scaling */
	public short dst_height;
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
	public byte[] videodata;
	int k = 0;
	int count = 0;

	Player player = null;
	CaptureDeviceInfo di = null;
	DataSource ds;
	DataSource clone;
	private MediaLocator locator;
	URL url;

	ThincProtocol proto;

	ThincVideo(String host, int port, ThincProtocol protocol)
			throws ThincException, NoPlayerException, CannotRealizeException,
			IOException {
		// this.host = host;
		// this.port = port;
		super(" Video Capture and Playback ");
		proto = protocol;
	}

	private void createPlayer(byte[] videodata) throws NoPlayerException,
			CannotRealizeException, IOException {
		// removePreviousPlayer();
		int dsize;
		VideoFormat vFormat = new VideoFormat(VideoFormat.YUV);
		LiveStream livestream = new LiveStream(videodata, Width, height,
				yuv_yoffset, yuv_uoffset, yuv_voffset, yuv_ypitch, yuv_upitch,
				size * 6000000, timestamp);
		DataSource ds = new DataSource(livestream);
		ds.connect();
		player = Manager.createPlayer(ds);
		player.addControllerListener(new EventHandler());
		start();
	}

	private void removePreviousPlayer() {
		if (player == null)
			return;
		stop();
		destroy();
	}

	public void start() {
		player.start();
	}

	public void stop() {
		player.stop();
		player.deallocate();
	}

	public void destroy() {
		player.close();

	}

	public void startVideo(SocketChannel t) throws ThincException {
		// TODO Auto-generated method stub
		sc = t;
		ByteBuffer bb = allocateHeaderByteBuffer(20);
		try {
			readBytes(sc, bb);
		} catch (IOException e) {
			throw new ThincException(
					"An error occurred while GETTING StartVideoData!!!", e);
		}
		bb.flip();
		id = bb.getInt(); /* id for this video stream */
		fmt_id = bb.getInt(); /*
								 * format. Only send id, which should have been
								 * set on initialization
								 */

		/* position */
		videox = bb.getShort();
		videoy = bb.getShort();

		/* dimensions */
		Width = bb.getShort();
		height = bb.getShort();

		/* for client scaling */
		dst_width = bb.getShort();
		dst_height = bb.getShort();

		/*
		 * followed by struct with format specific info (possible list below)
		 */
		ByteBuffer fi = allocateHeaderByteBuffer(24);
		try {
			readBytes(sc, fi);
		} catch (IOException e) {
			throw new ThincException(
					"An error occurred while GETTING StartVideoData!!!", e);
		}
		fi.flip();
		yuv_yoffset = fi.getInt();
		yuv_uoffset = fi.getInt();
		yuv_voffset = fi.getInt();
		yuv_ypitch = fi.getInt();
		yuv_upitch = fi.getInt();
		yuv_vpitch = fi.getInt();
		// TODO take care of other formats
		sendVideoStart(id);
		try {
			videodata = new byte[Math.abs(Width * height * 3)];
		} catch (Exception e) {
			e.getCause();
		}
	}

	public void nextVideo() throws ThincException, NoPlayerException,
			CannotRealizeException, IOException {
		count++;
		// TODO take care of compressed data
		ByteBuffer vdatainfo = allocateHeaderByteBuffer(12);
		try {
			readBytes(sc, vdatainfo);
		} catch (IOException e) {
			throw new ThincException(
					"An error occurred while GETTING StartVideoData!!!", e);
		}
		vdatainfo.flip();
		id = vdatainfo.getInt(); /* stream id */
		size = vdatainfo.getInt(); /* size of video data */
		timestamp = vdatainfo.getInt(); /* timestamp for synchronization */
		/* followed by video data */

		ByteBuffer vdata = allocateHeaderByteBuffer(size);
		try {
			readBytes(sc, vdata);
		} catch (IOException e) {
			throw new ThincException(
					"An error occurred while GETTING StartVideoData!!!", e);
		}
		vdata.flip(); // Seems like you shouldnt flip for retval

		// Original working code
		for (int i = 0; i < size; i++) {
			try {
				videodata[k++] = vdata.get();
			} catch (Exception e) {

			}
		}

		/*
		 * if(count==6000000){ createPlayer(videodata); setSize( dst_width,
		 * dst_height ); count = 0; k=0; } //show();
		 * 
		 */
		IntBuffer retVal = IntBuffer.allocate(Width * height);
		byte y0 = 0, v0 = 0, u0 = 0;
		int rgb0;
		int yVal, vVal, uVal;
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < Width; j++) {
				yVal = yuv_yoffset + (i * yuv_ypitch + j);
				vVal = yuv_voffset
						+ ((int) (i / 2) * (int) (yuv_vpitch) + (int) (j / 2));
				uVal = yuv_uoffset
						+ ((int) (i / 2) * (int) (yuv_upitch) + (int) (j / 2));
				if (yVal < vdata.limit())
					y0 = videodata[yVal];
				if (vVal < vdata.limit())
					v0 = videodata[vVal];
				if (uVal < vdata.limit())
					u0 = videodata[uVal];
				rgb0 = yuvToRgb(y0, u0, v0);
				retVal.put(rgb0);
			}
		}
		retVal.flip();

		if (retVal != null)
			proto.canvas.drawImageScaled(retVal, videox, videoy, Width, height,
					dst_width, dst_height, 4);
		//
	}

	private int yuvToRgb(byte y, byte u, byte v) {
		// TODO Auto-generated method stub
		short r, g, b;
		/* make y,u,v as unsigned value */
		short uy = unsigned(y);
		short uu = unsigned(u);
		short uv = unsigned(v);
		int retVal = 0;

		/* get the r,g,b value by using formulas below */
		r = (short) ((298 * (uy - 16) + 409 * (uv - 128) + 128) >> 8);
		g = (short) ((298 * (uy - 16) - 100 * (uu - 128) - 208 * (uv - 128) + 128) >> 8);
		b = (short) ((298 * (uy - 16) + 516 * (uu - 128) + 128) >> 8);

		/* if RGB is larger then 255 or smaller than 0, cut it out */
		if (r > 255)
			r = (short) 255;
		if (r < 0)
			r = (short) 0;
		if (g > 255)
			g = (short) 255;
		if (g < 0)
			g = (short) 0;
		if (b > 255)
			b = (short) 255;
		if (b < 0)
			b = (short) 0;

		/* set the r,g,b into one int type */
		retVal = 0xff;
		retVal = (retVal << 8) | b;
		retVal = (retVal << 8) | g;
		retVal = (retVal << 8) | r;
		return retVal;
	}

	private short unsigned(byte src) {
		return (short) (src & 0xFF);
	}

	public void endVideo() throws ThincException {
		// TODO Auto-generated method stub
		videodata = null;
		ByteBuffer vend = allocateHeaderByteBuffer(4);
		try {
			readBytes(sc, vend);
		} catch (IOException e) {
			throw new ThincException(
					"An error occurred while GETTING StartVideoData!!!", e);
		}
		vend.flip();
		id = vend.getInt(); /* stream id */
		// destroy();
	}

	public void moveVideo() throws ThincException {
		// TODO Auto-generated method stub
		ByteBuffer vmovedata = allocateHeaderByteBuffer(8);
		try {
			readBytes(sc, vmovedata);
		} catch (IOException e) {
			throw new ThincException(
					"An error occurred while GETTING StartVideoData!!!", e);
		}
		vmovedata.flip();
		id = vmovedata.getInt(); /* stream id */
		videox = vmovedata.getShort();
		videoy = vmovedata.getShort();
	}

	public void scaleVideo() throws ThincException {
		// TODO Auto-generated method stub
		ByteBuffer vrescale = allocateHeaderByteBuffer(8);
		try {
			readBytes(sc, vrescale);
		} catch (IOException e) {
			throw new ThincException(
					"An error occurred while GETTING StartVideoData!!!", e);
		}
		vrescale.flip();
		id = vrescale.getInt(); // :32;
		dst_width = vrescale.getShort(); // :16;
		dst_height = vrescale.getShort(); // :16;
		videodata = new byte[dst_width * dst_height * 3];
	}

	public void resizeVideo() throws ThincException {
		// TODO Auto-generated method stub

		ByteBuffer bb = allocateHeaderByteBuffer(8);
		try {
			readBytes(sc, bb);
		} catch (IOException e) {
			throw new ThincException(
					"An error occurred while GETTING StartVideoData!!!", e);
		}
		bb.flip();
		id = bb.getInt(); /* id for this video stream */
		/* dimensions */
		Width = bb.getShort();
		height = bb.getShort();
		ByteBuffer fi = allocateHeaderByteBuffer(24);

		videodata = new byte[Width * height * 3];
		try {
			readBytes(sc, fi);
		} catch (IOException e) {
			throw new ThincException(
					"An error occurred while GETTING StartVideoData!!!", e);
		}
		fi.flip();
		yuv_yoffset = fi.getInt();
		yuv_uoffset = fi.getInt();
		yuv_voffset = fi.getInt();
		yuv_ypitch = fi.getInt();
		yuv_upitch = fi.getInt();
		yuv_vpitch = fi.getInt();

	}

	void sendVideoStart(int videoId) throws ThincException {
		// logger1.debug("Entering sendVideoStart within Protocol");
		short length = 5;
		ByteBuffer bb = allocateByteBuffer(4 + 5);
		// message type
		bb.put((byte) 110);
		bb.put((byte) 0);
		bb.putShort(length);
		bb.putInt(videoId);
		bb.put(IS_VIDEO_OKAY);
		bb.flip();
		try {
			sendBytes(getSocketChannel(), bb);
		} catch (IOException e) {
			throw new ThincException("An error occurred while doing "
					+ "sendVideoStart", e);
		}
		// logger1.debug("Exiting sendVideoStart within Protocol");
	}

	// Utility methods same as from protocol
	public ByteBuffer allocateHeaderByteBuffer(int capacity) {
		return ByteBuffer.allocateDirect(capacity).order(ByteOrder.BIG_ENDIAN);
	}

	ByteBuffer allocateByteBuffer(int capacity) {
		// logger1.debug("Entering and exiting allocateByteBuffer within
		// Protocol");
		return ByteBuffer.allocate(capacity).order(defaultByteOrder);
	}

	static int readBytes(SocketChannel sc, ByteBuffer bb) throws IOException {
		int bytesRead = 0;
		while (bb.hasRemaining()) {
			bytesRead += sc.read(bb);
		}
		return bytesRead;
	}

	SocketChannel getSocketChannel() {
		return sc;
	}

	static int sendBytes(SocketChannel sc, ByteBuffer bb) throws IOException {
		// logger1.debug("Entering sendBytes within Protocol");
		int bytesSent = 0;
		while (bb.hasRemaining()) {
			bytesSent += sc.write(bb);
		}
		return bytesSent;
	}

	// inner class to handler events from media player
	private class EventHandler implements ControllerListener {
		public synchronized void controllerUpdate(ControllerEvent e) {
			if (e instanceof RealizeCompleteEvent) {
				Container c = getContentPane();

				// load Visual and Control components if they exist
				Component visualComponent = player.getVisualComponent();

				if (visualComponent != null)
					c.add(visualComponent, BorderLayout.CENTER);

				Component controlsComponent = player.getControlPanelComponent();

				if (controlsComponent != null)
					c.add(controlsComponent, BorderLayout.SOUTH);
				// validate();
				c.doLayout();
			}
		}
	}

}
