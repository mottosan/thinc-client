/*********************************************
 * Thinc Client
 * Amortya Ray & Divya Arora
 * {ar2566, da2254} @columbia.edu
 * Fall 2006-Fall 2007
 *********************************************/

package thinc;

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

public class ThincLogger extends Logger {
	private static ThincLoggerFactory tcLoggerFactory = new ThincLoggerFactory();

	ThincLogger(String clazz) {
		super(clazz);
	}

	public static ThincLogger getATSLogger(String name) {
		return (ThincLogger) Logger.getLogger(name, tcLoggerFactory);
	}

	public static ThincLogger getATSLogger(Class clazz) {
		return (ThincLogger) Logger.getLogger(clazz.getName(), tcLoggerFactory);
	}

	public void debug(Object object, Throwable exception) {
		super.debug(object, exception);
	}

	public void debug(Object object) {
		super.debug(object);
	}

	public void error(Object object, Throwable exception) {
		super.error(object, exception);
	}

	public void error(Object object) {
		super.error(object);
	}

	public void fatal(Object object, Throwable exception) {
		super.fatal(object, exception);
	}

	public void fatal(Object object) {
		super.fatal(object);
	}

	public void info(Object object, Throwable exception) {
		super.info(object, exception);
	}

	public void info(Object object) {
		super.info(object);
	}

	public void log(Priority priority, Object object, Throwable throwable) {
		super.log(priority, object, throwable);
	}

	public void log(Priority object, Object exception) {
		super.log(object, exception);
	}

	public void warn(Object object, Throwable exception) {
		super.warn(object, exception);
	}

	public void warn(Object object) {
		super.warn(object);
	}

	public boolean isDebugEnabled() {
		return super.isDebugEnabled();
	}

	public boolean isInfoEnabled() {
		return super.isInfoEnabled();
	}

}
