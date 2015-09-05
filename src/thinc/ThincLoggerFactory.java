/*********************************************
 * Thinc Client
 * Amortya Ray & Divya Arora
 * {ar2566, da2254} @columbia.edu
 * Fall 2006-Fall 2007
 *********************************************/

package thinc;

import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggerFactory;

public class ThincLoggerFactory implements LoggerFactory {
	/**
	 * Create a new Logger instance.
	 * 
	 * @param name
	 *            Name(Context) of the logger.
	 */
	public Logger makeNewLoggerInstance(String name) {
		return new ThincLogger(name);
	}

}
