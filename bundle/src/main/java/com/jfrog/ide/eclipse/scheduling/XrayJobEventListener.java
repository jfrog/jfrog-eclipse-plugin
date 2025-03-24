package com.jfrog.ide.eclipse.scheduling;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;

public class XrayJobEventListener extends JobChangeAdapter {

	@Override
	public void done(IJobChangeEvent event) {
		Job[] jobs = Job.getJobManager().find(ScanJob.FAMILY);
		// TODO: implement a listener for the audit scan
//		ScanManagersFactory scanManagersFactory = ScanManagersFactory.getInstance(); 
		if (ArrayUtils.isEmpty(jobs)) {
//			scanManagersFactory.scanFinished();
		}
	}
}
