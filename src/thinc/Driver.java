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

public class Driver extends Applet {
	public boolean appletflag = false;

	// static constants
	private static final String THINC_APP_NAME = "Thinc Client";
	// private static final String HOST = "cluster05.ncl.cs.columbia.edu";
	private static final String HOST = "128.59.19.219";
	private static final int PORT = 20000;
	private static final String USAGE = "\n\nUsage: ThincClient <host> <port>, "
			+ "e.g. 'ThincClient myhost.com 20000'\n";

	// member variables
	private ThincProtocol proto;
	// reference to ThincClient needed for when client starts as an Applet
	private ThincClient client;
	private String host;
	private int port;

	/**
	 * Main method for running as a standalone application.
	 */
	public static void main(String[] args) {

		// logger.debug("Entering Main Method!");

		// parse parameters and assert them
		if (null == args || args.length == 0) {
			System.out.println("Could not find arguments to parse." + USAGE);
			System.exit(1);
		}

		if (args.length == 1) {
			System.out.println("Need two arguments to startup client." + USAGE);
			System.exit(1);
		}

		String host = args[0];
		if (null == host || host.trim().length() == 0) {
			System.out.println("Cannot instantiate ThincClient with a "
					+ "host parameter!");
			System.exit(1);
		}

		int port = -1;
		try {
			port = Integer.parseInt(args[1]);
		} catch (NumberFormatException e) {
			System.out.println("Specified port '" + args[1] + "' is not a "
					+ "valid integer." + USAGE);
			System.exit(1);
		}

		if (port < 0) {
			System.out
					.println("Port number " + port + " is not valid." + USAGE);
			System.exit(1);
		}
		// logger.debug("Creating a new object of the type ThincClient");
		ThincClient client = new ThincClient(host, port);
		client.start();
	}

	// ---------------------------- Applet methods
	// ----------------------------//
	public void start() {
		// logger.debug("Entering start method");
		String errString = null;
		try {
			proto = new ThincProtocol(THINC_APP_NAME, client.host, client.port,
					appletflag, this);

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

	public void init() {
		// Called once by the browser when it starts the applet
		System.out.println("WITHIN INIT");
		client = new ThincClient(HOST, PORT);
		appletflag = true;
	}
}
