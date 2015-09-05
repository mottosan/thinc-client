/*********************************************
 * Thinc Client
 * Amortya Ray & Divya Arora
 * {ar2566, da2254} @columbia.edu
 * Fall 2006-Fall 2007
 *********************************************/

package thinc;

/**
 * Defines an interface to allow an Exception to wrap another Exception and
 * guarantee that clients can test for and access those nested Exceptions.
 * Notably implemented by <code>{@link ExceptionWrapper}</code>, but the
 * existence of this interface allows any Exception to implement it with any
 * constructors or access methods.
 * 
 * @see ExceptionWrapper
 */

public interface WrappableException {

	/**
	 * Allow access to the enclosed Exception.
	 * 
	 * @return null if no enclosed Exception.
	 */

	public Exception getEnclosedException();

}
