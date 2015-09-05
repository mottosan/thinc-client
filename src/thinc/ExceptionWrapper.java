/*********************************************
 * Thinc Client
 * Amortya Ray & Divya Arora
 * {ar2566, da2254} @columbia.edu
 * Fall 2006-Fall 2007
 *********************************************/

package thinc;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.sql.SQLException;

/**
 * Serves as a base class for <code>{@link WrappableException}</code>s, to
 * allow exception nesting. Extend this class for your custom service exception
 * case. Alternately, if you don't like the constructors or methods of this
 * exception, implement <code>{@link WrappableException}</code> to create your
 * own -- it's nesting behavior will be compatible with this class, allowing
 * interchangable nesting of either without clunky subclassing behavior.
 * <p>
 * Inside your service, catch low-level exceptions and re-throw them wrapped in
 * your custom exception. This allows simplification of clients who need to
 * catch exceptions from your service, but doesn't sacrifice the level of detail
 * represented by the wrapped exception, since it is accessible via
 * {@link #getEnclosedException}.
 * <p>
 * For example, if you've created a custom "MyServiceException" that extends
 * either this Exception, or, alternately, implements the WrapperableException
 * interface, when using it the internals of your service might look like:
 * 
 * <pre>
 * try {
 * 	someOperationWhichThrowsAnException();
 * } catch (SomeLowLevelException ex) {
 * 	throw new MyServiceException(&quot;Something jiggy is up while doing this!&quot;, ex);
 * }
 * </pre>
 * 
 * A client, after catching "MyServiceException", can either extract the
 * enclosed exception or call methods on this exception, like {@link #toString},
 * to derive its internals and save them to a log, email them to a developer, or
 * display them to a user.
 * <p>
 * The exception classnames and messages are number 1 to N, from outermost to
 * innermost. The innermost also has an appended call stack trace to allow
 * pinpointing the source of the exception.
 * 
 * @author Leo Kim (lnk2101@columbia.edu)
 * @see WrappableException
 */

public class ExceptionWrapper extends Exception implements WrappableException {

	private Exception nestedException = null;

	/**
	 * Constructs a ExceptionWrapper with no specific message.
	 */
	public ExceptionWrapper() {
		super();
	}

	/**
	 * Constructs an ExceptionWrapper with the specified message.
	 */
	public ExceptionWrapper(String desc) {
		super(desc);
	}

	/**
	 * Constructs an ExceptionWrapper with the specified message and wraps the
	 * specified Exception which caused the problem.
	 * 
	 * @param desc
	 *            a <code>String</code> representing the description of the
	 *            problem that occurred
	 * @param e
	 *            <code>Exception</code> to be wrapped
	 */
	public ExceptionWrapper(String desc, Exception e) {
		super(desc);
		nestedException = e;
	}

	/**
	 * Constructs an ExceptionWrapper with no specific message and wraps the
	 * specified Exception which caused the problem.
	 * 
	 * @param e
	 *            <code>Exception</code> which caused the problem
	 */
	public ExceptionWrapper(Exception e) {
		super();
		nestedException = e;
	}

	/**
	 * Allow access to enclosed exception.
	 * 
	 * @return enclosed <code>Exception</code>, may be null
	 */
	public Exception getEnclosedException() {
		return nestedException;
	}

	/**
	 * Allow access to messages and stack trace of this and any enclosed
	 * exception, including nested SQLExceptions.
	 * 
	 * @return exception names, message and stack trace for this and enclosed
	 *         exception. If enclosed exception implements WrapperableException,
	 *         its name and message is appended and recursion continues. If
	 *         enclosed exception is a SQLException, its name and message is
	 *         appended and recursion continues. Only the deepest exception
	 *         appends a stack trace. See class level documentation for an
	 *         example of this output.
	 */
	public String toString() {
		StringBuffer msg = new StringBuffer();
		// Date now = new Date(); // so exception chain is somewhat unique
		int depth = 1;
		msg.append("\n");
		msg.append("* Exception Chain START ****************************");
		msg.append("\n\n");
		msg.append(depth + ") " + getClass().getName() + ": " + getMessage()
				+ "\n");
		Exception enclosedException = getEnclosedException();

		// Loop through all enclosed exceptions
		while (enclosedException != null) {
			depth++;

			// Enclosures?
			if (enclosedException instanceof WrappableException) {
				WrappableException exceptionWrapper = (WrappableException) enclosedException;
				Exception nextEnclosedException = exceptionWrapper
						.getEnclosedException();
				// only stack trace inner most exception
				if (nextEnclosedException == null) {
					msg.append(depth + ") "
							+ getStackTraceAsString(enclosedException) + "\n");
				} else {
					msg.append(depth + ") " + getClassName(enclosedException)
							+ ": " + enclosedException.getMessage() + "\n");
				}
				// fillerup for next loop
				enclosedException = nextEnclosedException;
			} else if (enclosedException instanceof SQLException) {
				// SQLExceptions also allow nesting, but use a different method
				// signature
				SQLException sqlException = (SQLException) enclosedException;
				Exception nextSqlException = sqlException.getNextException();

				msg.append(depth + ") " + getClassName(enclosedException)
						+ ": SQL Error Code: " + sqlException.getErrorCode()
						+ ": " + enclosedException.getMessage() + "\n");
				if (nextSqlException == null) {
					// only stack trace inner most exception
					msg.append("Stack trace: "
							+ getStackTraceAsString(enclosedException) + "\n");
				}
				// fillerup for next loop
				enclosedException = nextSqlException;
			} else {
				msg.append(depth + ") "
						+ getStackTraceAsString(enclosedException) + "\n");
				enclosedException = null; // end loop
			}
		}
		return msg.toString();
	}

	/**
	 * Gets an exception's stack trace as a String :NOTE: this could be moved to
	 * a utils class
	 * 
	 * @param e
	 *            the exception
	 * @return the stack trace of the exception
	 */
	protected static String getStackTraceAsString(Exception e) {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		PrintWriter writer = new PrintWriter(bytes, true);
		e.printStackTrace(writer);
		writer.println();
		return bytes.toString();
	}

	/**
	 * Helper to get name of Class of the given Object. :NOTE: this could be
	 * moved to a utils class
	 * 
	 * @param o
	 *            the Object to derive the class name from.
	 * @return the class name of the given Object. Return empty String if passed
	 *         null.
	 */
	protected static String getClassName(Object o) {
		return (o != null) ? o.getClass().getName() : new String();
	}

	/**
	 * Overriden method from Throwable. Prints stack trace correctly of the
	 * exception that is stored (wrapped) in this class. Prints messages
	 * (getMessage()) of all WrapperableException-s and prints stacktrace of the
	 * wrapped exception. For details on this message see
	 * {@link java.lang.Throwable}.
	 * 
	 * @see java.lang.Throwable#printStackTrace(PrintWriter)
	 */
	public void printStackTrace(PrintWriter s) {
		Exception enclosedException = getEnclosedException();
		if (enclosedException != null) {
			s.println("WrappableException (" + getClassName(this) + "): "
					+ getMessage() + "\n");
			s.flush();
			// print stack trace of the enclosed exception and recurse
			enclosedException.printStackTrace(s);
			s.flush();
		} else {
			// has no enclosed exception, print the stack trace of this
			// exception
			// most likely will result in no printout
			s.println("WrappableException (" + getClassName(this) + "): "
					+ getMessage() + "\n");
			s.flush();
			super.printStackTrace(s);
			s.flush();
		}
	}

	/**
	 * Overriden method from Throwable. Prints stack trace correctly of the
	 * exception that is stored (wrapped) in this class. Prints messages
	 * (getMessage()) of all <code>WrappableException</code>s and prints
	 * stacktrace of the wrapped exception. For details on this message see
	 * <code>{@link java.lang.Throwable}</code>.
	 * 
	 * @see java.lang.Throwable#printStackTrace(PrintStream)
	 */
	public void printStackTrace(PrintStream s) {
		printStackTrace(new PrintWriter(s));
	}

	/**
	 * Overriden method from Throwable. Prints stack trace correctly of the
	 * exception that is stored (wrapped) in this class. Prints messages
	 * (getMessage()) of all WrapperableException-s and prints stacktrace of the
	 * wrapped exception. For details on this message see <code>
	 * {@link java.lang.Throwable}</code>.
	 * 
	 * @see java.lang.Throwable#printStackTrace()
	 */
	public void printStackTrace() {
		printStackTrace(System.err);
	}
}