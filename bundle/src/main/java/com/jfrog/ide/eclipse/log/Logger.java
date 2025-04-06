package com.jfrog.ide.eclipse.log;

import org.codehaus.plexus.util.ExceptionUtils;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Status;
import org.jfrog.build.api.util.Log;

public class Logger implements Log {
	private static final long serialVersionUID = 1L;
	private static ILog ilog;
	private static Logger instance;
	private static ProblemsLogger viewLogger;
	private final String ID = "jfrog-eclipse-plugin";
	
	public static final int DEBUG = 1;
	public static final int INFO = 2;
	public static final int WARN = 3;
	public static final int ERROR = 4;
	
	//set default log level as INFO
	private int logLevel = INFO;

	private Logger() {
		ilog = ResourcesPlugin.getPlugin().getLog();
	}

	public static Logger getInstance() {
		if (instance == null) {
			instance = new Logger();
		}
		return instance;
	}
	
	public void setLogLevel(int logLevel) {
	    this.logLevel = logLevel;
	}

	public int getLogLevel() {
	    return logLevel;
	}

	@Override
	public void debug(String message) {
		log(DEBUG, Status.OK, "[DEBUG] ", message);
	}

	@Override
	public void error(String message) {
		log(ERROR, Status.ERROR, "[ERROR] ", message);
	}

	@Override
	public void error(String message, Throwable t) {
		error(message);
		ExceptionUtils.printRootCauseStackTrace(t);
	}

	@Override
	public void info(String message) {
		log(INFO, Status.INFO, "[INFO] ", message);
	}

	@Override
	public void warn(String message) {
		log(WARN, Status.WARNING, "[WARN] ", message);
	}
	
    private void log(int level, int status, String prefix, String message) {
        if (logLevel <= level) {
            ilog.log(new Status(status, ID, prefix + message));
            if (status == Status.WARNING || status == Status.ERROR) {
                logToProblemsLogger(message, status);
            }
        }
    }

	private void logToProblemsLogger(String message, int status) {
		if (viewLogger == null) {
				viewLogger = ProblemsLogger.getInstance();
		}
		switch (status) {
		case Status.WARNING:
			viewLogger.warn(message);
			break;
		case Status.ERROR:
			viewLogger.error(message);
			break;
		default:
			ilog.log(new Status(Status.ERROR, ID, "[ERROR] Got unknown status: " + status));
		}
	}
}
