package com.jfrog.ide.eclipse.scheduling;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;

import com.jfrog.ide.eclipse.scan.ScanManager;

public class CliJobEventListener extends JobChangeAdapter {

	@Override
	public void done(IJobChangeEvent event) {
		Job[] jobs = Job.getJobManager().find(CliJob.FAMILY);
		if (ArrayUtils.isEmpty(jobs)) {
			ScanManager.getInstance().scanFinished();
		}
	}
}
