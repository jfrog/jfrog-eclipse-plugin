package org.jfrog.eclipse.scheduling;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

/**
 * Run a job Asynchronously and Serially.
 * 
 * @author yahavi
 */
public class ScanJob extends Job {

	private ICoreRunnable runnable;
	public static String FAMILY = "JFrogEclipsePluginJob";

	public ScanJob(String name, ICoreRunnable runnable) {
		super(name);
		setUser(true);
		setRule(ResourcesPlugin.getWorkspace().getRoot());
		addJobChangeListener(new XrayJobEventListener());
		this.runnable = runnable;
	}

	/**
	 * Schedule a job.
	 * 
	 * @param name - Job's name.
	 * @param runnable - Job's callback.
	 */
	public static void doSchedule(String name, ICoreRunnable runnable) {
		new ScanJob(name, runnable).schedule();
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		try {
			runnable.run(monitor);
		} catch (CoreException e) {
			IStatus st = e.getStatus();
			return new Status(st.getSeverity(), st.getPlugin(), st.getCode(), st.getMessage(), e);
		}
		return Status.OK_STATUS;
	}

	@Override
	public boolean belongsTo(Object family) {
		return FAMILY.equals(family);
	}
}
