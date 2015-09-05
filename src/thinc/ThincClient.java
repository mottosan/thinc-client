/*********************************************
 * Thinc Client
 * Amortya Ray & Divya Arora
 * {ar2566, da2254} @columbia.edu
 * Fall 2006-Fall 2007
 *********************************************/

package thinc;

import java.applet.Applet;

/**
 * Java THINC Client. Contains main method and other methods to bootstrap
 * client. Also doubles as a Java <code>{@link Applet}</code>.
 */

public class ThincClient {
	// public static Logger logger = ThincLogger.getLogger(ThincClient.class);
	// static constants
	private static final String THINC_APP_NAME = "Thinc Client";
	// private static final String HOST = "cluster05.ncl.cs.columbia.edu";
	private static final String HOST = "blade14.ncl.cs.columbia.edu";
	private static final int PORT = 20000;
	private static final String USAGE = "\n\nUsage: ThincClient <host> <port>, "
			+ "e.g. 'ThincClient myhost.com 20000'\n";

	// member variables
	private ThincProtocol proto;
	// reference to ThincClient needed for when client starts as an Applet
	public ThincClient client;
	public String host;
	public int port;

	/**
	 * Main method for running as a standalone application.
	 */

	public ThincClient(String host, int port) {
		this.host = host;
		this.port = port;

	}

	public void start() {

		// logger.debug("Entering start method");
		String errString = null;
		try {
			proto = new ThincProtocol(THINC_APP_NAME, host, port, false, null);

			// logger.debug("Created new object proto of type ThincPrototype");
			// logger.debug("Calling start eventloop for proto");
			proto.run(); // startEventLoop();
			// logger.debug("Back from start event loop and end of start
			// method");
		} catch (ThincException e) {
			errString = e.toString();
		} catch (Exception e) {
			// all other exceptions should be wrapped by this clause
			ThincException te = new ThincException("Unexpected error in event "
					+ "loop.", e);
			errString = te.toString();
		}

		if (null != errString) {
			System.out.println(errString);
		}

		if (null != proto) {
			proto.close();
		}
		System.out.println("Exiting.");
		System.exit(0);

	}
}