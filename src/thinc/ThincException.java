/*********************************************
 * Thinc Client
 * Amortya Ray & Divya Arora
 * {ar2566, da2254} @columbia.edu
 * Fall 2006-Fall 2007
 *********************************************/

package thinc;

/**
 * ThincException. Base exception for all ThincExceptions, which are checked.
 * 
 * @see ExceptionWrapper
 */

public class ThincException extends ExceptionWrapper {

	/**
	 * Allow printing a message regarding details of the context of this
	 * exception.
	 * 
	 * @param message
	 *            the message to display when
	 *            {@link java.lang.Exception#getMessage} is used.
	 */
	public ThincException(String message) {
		super(message);
	}

	/**
	 * Allow an enclosed exception to hold the details of the context of this
	 * exception.
	 * 
	 * @param e
	 *            the exception to enclose
	 */
	public ThincException(Exception e) {
		super(e);
	}

	/**
	 * Allow an enclosed exception to hold a message and the details of the
	 * context of this exception.
	 * 
	 * @param message
	 *            the message to display when <code>getMessage<code> is
	 * used.
	 * @param e the exception to enclose
	 */
	public ThincException(String message, Exception e) {
		super(message, e);
	}
}
