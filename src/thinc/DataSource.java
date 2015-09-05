/*********************************************
 * Thinc Client
 * Amortya Ray & Divya Arora
 * {ar2566, da2254} @columbia.edu
 * Fall 2006-Fall 2007
 *********************************************/

package thinc;

// package jmfsample.media.protocol.live;

import javax.media.Time;
import javax.media.MediaLocator;
import javax.media.protocol.*;
import java.io.IOException;

public class DataSource extends PushBufferDataSource {

	protected Object[] controls = new Object[0];
	protected boolean started = false;
	protected String contentType = "raw";
	protected boolean connected = false;
	protected Time duration = DURATION_UNKNOWN;
	protected LiveStream[] streams = new LiveStream[10000000];
	protected LiveStream stream;
	protected String name;
	int count = 0;

	public DataSource(LiveStream ls) {
		stream = streams[(count++ % 10000000)] = ls;
	}

	public String getContentType() {
		if (!connected) {
			System.err.println("Error: DataSource not connected");
			return null;
		}
		return contentType;
	}

	public void connect() throws IOException {
		if (connected)
			return;
		connected = true;
	}

	public void disconnect() {
		try {
			if (started)
				stop();
		} catch (IOException e) {
		}
		connected = false;
	}

	public void start() throws IOException {
		// we need to throw error if connect() has not been called
		if (!connected)
			throw new java.lang.Error(
					"DataSource must be connected before it can be started");
		if (started)
			return;
		started = true;
		stream.start(true);
	}

	public void stop() throws IOException {
		if ((!connected) || (!started))
			return;
		started = false;
		stream.start(false);
	}

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

	public Time getDuration() {
		return duration;
	}

	public PushBufferStream[] getStreams() {
		if (streams == null) {
			streams = new LiveStream[1];
			streams[0] = stream;
		}
		return streams;
	}

}
