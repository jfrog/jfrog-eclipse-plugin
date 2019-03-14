package org.jfrog.eclipse.scheduling;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.jfrog.eclipse.scan.ScanManagersFactory;

public class XrayJobEventListener extends JobChangeAdapter {

	@Override
	public void done(IJobChangeEvent event) {
		Job[] jobs = Job.getJobManager().find(ScanJob.FAMILY);
		ScanManagersFactory scanManagersFactory = ScanManagersFactory.getInstance();
		if (jobs.length == 0) {
			scanManagersFactory.setScanInProgress(false);
		}
	}
}
