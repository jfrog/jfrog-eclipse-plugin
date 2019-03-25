package com.jfrog.ide.eclipse.scan;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;

import com.jfrog.ide.eclipse.scheduling.ScanJob;
import com.jfrog.ide.eclipse.utils.Utils;

import junit.framework.TestCase;

public class ScanManagerTest extends TestCase {

	public void testSchedulingAJob() throws IOException, CoreException, OperationCanceledException, InterruptedException {
		String projectName = "gradleIsApplicable";
		JobListener jobListener = new JobListener();
		IProject project = Utils.createProject(projectName, "gradle");
		ScanManager scanManager = new GradleScanManager(project);
		Job.getJobManager().addJobChangeListener(jobListener);
		scanManager.scanAndUpdateResults(false, null, null, null);
		Job.getJobManager().join(ScanJob.FAMILY, new NullProgressMonitor());
		assertJobInformation(projectName, jobListener);
		cleanup(jobListener);
	}

	public void testScanFinished() throws IOException, CoreException, OperationCanceledException, InterruptedException {
		ScanManagersFactory.getInstance().getScanInProgress().set(true);
		String projectName = "gradleIsApplicable";
		JobListener jobListener = new JobListener();
		Job.getJobManager().addJobChangeListener(jobListener);
		IProject project = Utils.createProject(projectName, "gradle");
		ScanManager scanManager = new GradleScanManager(project);
		scanManager.scanAndUpdateResults(false, null, null, null);
		Job.getJobManager().join(ScanJob.FAMILY, new NullProgressMonitor());
		assertJobInformation(projectName, jobListener);
		assertFalse(ScanManagersFactory.getInstance().getScanInProgress().get());
		cleanup(jobListener);
	}

	private void cleanup(JobListener jobListener) {
		JobListener.jobExists.set(false);
		JobListener.numOfJobs.set(0);
		Job.getJobManager().removeJobChangeListener(jobListener);
	}

	private void assertJobInformation(String projectName, JobListener jobListener) {
		assertTrue(jobListener.isJobExists());
		assertEquals(projectName, jobListener.getJobName());
		assertEquals(1, jobListener.numOfJobs());
	}

	static class JobListener extends JobChangeAdapter {

		private static AtomicBoolean jobExists = new AtomicBoolean(false);
		private String jobName;
		private static AtomicInteger numOfJobs = new AtomicInteger();
		
		@Override 
		public void scheduled(IJobChangeEvent event) {
			if (event.getJob().belongsTo(ScanJob.FAMILY)) {
				this.jobName = event.getJob().getName();
				numOfJobs.incrementAndGet();
			}
		}
		@Override
		public void done(IJobChangeEvent event) {
			// TODO Auto-generated method stub
			if (event.getJob().belongsTo(ScanJob.FAMILY)) {
				jobExists.set(true);
			}
		}
		
		public boolean isJobExists() {
			return jobExists.get();
		}
		
		public String getJobName() {
			return jobName;
		}
		
		public int numOfJobs() {
			return numOfJobs.get();
		}
	}
}
