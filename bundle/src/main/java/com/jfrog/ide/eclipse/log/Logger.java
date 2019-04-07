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

	private Logger() {
		ilog = ResourcesPlugin.getPlugin().getLog();
	}

	public static Logger getInstance() {
		if (instance == null) {
			instance = new Logger();
		}
		return instance;
	}

	@Override
	public void debug(String message) {
		ilog.log(new Status(Status.OK, ID, "[OK] " + message));
	}

	@Override
	public void error(String message) {
		ilog.log(new Status(Status.ERROR, ID, "[ERROR] " + message));
		logToProblemsLogger(message, Status.ERROR);
	}

	@Override
	public void error(String message, Throwable t) {
		error(message);
		ExceptionUtils.printRootCauseStackTrace(t);
	}

	@Override
	public void info(String message) {
		ilog.log(new Status(Status.INFO, ID, "[INFO] " + message));
	}

	@Override
	public void warn(String message) {
		ilog.log(new Status(Status.WARNING, ID, "[WARN] " + message));
		logToProblemsLogger(message, Status.WARNING);
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
