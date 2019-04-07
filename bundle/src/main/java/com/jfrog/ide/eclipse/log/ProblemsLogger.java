package com.jfrog.ide.eclipse.log;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

public class ProblemsLogger {

	private static ProblemsLogger instance;

	public ProblemsLogger() {
	}

	public static ProblemsLogger getInstance() {
		if (instance == null) {
			instance = new ProblemsLogger();
		}
		return instance;
	}

	public void error(String message) {
		// Write to problems:
		writeMessage(message, IMarker.SEVERITY_ERROR);
	}
	
	public void warn(String message) {
		// Write to problems:
		writeMessage(message, IMarker.SEVERITY_WARNING);
	}

	private void writeMessage(String message, int severity) {
		IWorkspaceRunnable editorMarker = new WorkspaceRunnabe(message, severity);
		try {
			ResourcesPlugin.getWorkspace().run(editorMarker, new NullProgressMonitor());
		} catch (CoreException e) {
			Logger.getInstance().info("Some error set marker: " + e);
		}
	}
	
	class WorkspaceRunnabe implements IWorkspaceRunnable {
		
		private String message;
		private int severity;
		
		public WorkspaceRunnabe(String message, int severity) {
			this.message = message;
			this.severity = severity;
		}

		@Override
		public void run(IProgressMonitor monitor) throws CoreException {
			IMarker marker = ResourcesPlugin.getWorkspace().getRoot().createMarker(IMarker.PROBLEM);
			marker.setAttribute(IMarker.MESSAGE, message);
			marker.setAttribute(IMarker.LOCATION, "jfrog-eclipse-plugin");
			marker.setAttribute(IMarker.TRANSIENT, true);
			marker.setAttribute(IMarker.SEVERITY, severity);
		}
	}
}
