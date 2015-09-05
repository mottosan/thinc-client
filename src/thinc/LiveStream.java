/*********************************************
 * Thinc Client
 * Amortya Ray & Divya Arora
 * {ar2566, da2254} @columbia.edu
 * Fall 2006-Fall 2007
 *********************************************/

package thinc;

// package jmfsample.media.protocol.live;

import java.awt.Dimension;
import javax.media.*;
import javax.media.format.*;
import javax.media.protocol.*;
import javax.media.protocol.DataSource;
import java.io.IOException;

public class LiveStream implements PushBufferStream, Runnable {

	protected ContentDescriptor cd = new ContentDescriptor(
			ContentDescriptor.RAW);
	protected int maxDataLength;
	protected byte[] data;
	protected Dimension framesize;
	protected YUVFormat yuvFormat;
	protected AudioFormat audioFormat;
	protected boolean started;
	protected Thread thread;
	protected float frameRate = 60; // 20f;
	protected BufferTransferHandler transferHandler;
	protected Control[] controls = new Control[0];
	public byte[] videodata;
	protected boolean videoData = true;
	public int size;
	public int timestamp;

	public LiveStream(byte[] videodat, short width, short height,
			int yuv_yoffset, int yuv_uoffset, int yuv_voffset, int yuv_ypitch,
			int yuv_upitch, int siz, int timestmp) {
		videodata = videodat;
		size = siz;
		timestamp = timestmp;
		if (videoData) {

			framesize = new Dimension(width, height);
			maxDataLength = width * height * 3;
			int w = (int) width;
			yuvFormat = new YUVFormat(framesize, maxDataLength,
					Format.byteArray, frameRate, 2, yuv_ypitch, yuv_upitch,
					yuv_yoffset, yuv_uoffset, yuv_voffset);
		} else { // audio data
			audioFormat = new AudioFormat(AudioFormat.LINEAR, 8000.0, 8, 1,
					Format.NOT_SPECIFIED, AudioFormat.SIGNED, 8,
					Format.NOT_SPECIFIED, Format.byteArray);
			maxDataLength = 1000;
		}

		thread = new Thread(this);
		thread.run();
	}

	/***************************************************************************
	 * SourceStream
	 **************************************************************************/

	public ContentDescriptor getContentDescriptor() {
		return cd;
	}

	public long getContentLength() {
		return LENGTH_UNKNOWN;
	}

	public boolean endOfStream() {
		return false;
	}

	/***************************************************************************
	 * PushBufferStream
	 **************************************************************************/

	int seqNo = 0;
	double freq = 2.0;

	public Format getFormat() {
		if (videoData)
			return yuvFormat;
		else
			return audioFormat;
	}

	public void read(Buffer buffer) throws IOException {
		synchronized (this) {
			Object outdata = buffer.getData();
			if (outdata == null || !(outdata.getClass() == Format.byteArray)
					|| ((byte[]) outdata).length < size) {
				outdata = new byte[size];
				buffer.setData(videodata);
			}
			if (videoData) {
				buffer.setFormat(yuvFormat);
				buffer.setTimeStamp(timestamp);
				System.arraycopy(videodata, 0, outdata, 0, size);
			} else {
				buffer.setFormat(audioFormat);
				buffer.setTimeStamp(timestamp);
			}
			buffer.setSequenceNumber(seqNo);
			buffer.setLength(size);
			buffer.setFlags(0);
			buffer.setHeader(null);
			seqNo++;
		}
	}

	public void setTransferHandler(BufferTransferHandler transferHandler) {
		synchronized (this) {
			this.transferHandler = transferHandler;
			notifyAll();
		}
	}

	void start(boolean started) {
		synchronized (this) {
			this.started = started;
			if (started && !thread.isAlive()) {
				thread = new Thread(this);
				thread.start();
			}
			notifyAll();
		}
	}

	/***************************************************************************
	 * Runnable
	 **************************************************************************/

	public void run() {
		while (started) {
			synchronized (this) {
				while (transferHandler == null && started) {
					try {
						wait(1000);
					} catch (InterruptedException ie) {
					}
				} // while
			}

			if (started && transferHandler != null) {
				transferHandler.transferData(this);
				try {
					Thread.currentThread().sleep(10);
				} catch (InterruptedException ise) {
				}
			}
		} // while (started)
	} // run

	// Controls

	public Object[] getControls() {
		return controls;
	}

	public Object getControl(String controlType) {
		try {
			Class cls = Class.forName(controlType);
			Object cs[] = getControls();
			for (int i = 0; i < cs.length; i++) {
				if (cls.isInstance(cs[i]))
					return cs[i];
			}
			return null;

		} catch (Exception e) { // no such controlType or such control
			return null;
		}
	}
}
