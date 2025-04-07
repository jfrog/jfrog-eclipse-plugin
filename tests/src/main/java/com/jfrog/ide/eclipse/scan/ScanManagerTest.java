package com.jfrog.ide.eclipse.scan;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.swt.widgets.Composite;

import com.jfrog.ide.common.nodes.FileTreeNode;
import com.jfrog.ide.eclipse.scheduling.CliJob;
import com.jfrog.ide.eclipse.utils.Utils;

import junit.framework.TestCase;

public class ScanManagerTest extends TestCase {
	private ScanManager scanManager = ScanManager.getInstance();
	private ScanCache scanCache = ScanCache.getInstance();

	
	// TODO: generate test for scanning: Maven, Gradle and NPM projects. 
	// TODO: add resources dir with example projects to scan
	public void testScanMavenProject() throws IOException, CoreException {
		String projectName = "mavenIsApplicable";
		JobListener jobListener = new JobListener();
		IProject project = Utils.createProject(projectName, "maven");
		Job.getJobManager().addJobChangeListener(jobListener);
		
		scanManager.startScan(null, false);
		List<FileTreeNode> scanResults = scanCache.getScanResults();
		
	}
	
	public void testSchedulingAJob()
			throws IOException, CoreException, OperationCanceledException, InterruptedException {
		String projectName = "gradleIsApplicable";
		JobListener jobListener = new JobListener();
		IProject project = Utils.createProject(projectName, "gradle");
		Job.getJobManager().addJobChangeListener(jobListener);
		scanManager.scanAndUpdateResults(null, new Composite(null, 0), null, null);
		Job.getJobManager().join(CliJob.FAMILY, new NullProgressMonitor());
		assertJobInformation(projectName, jobListener);
		cleanup(jobListener);
	}

	public void testScanFinished() throws IOException, CoreException, OperationCanceledException, InterruptedException {
		scanManager.getScanInProgress().set(true);
		String projectName = "gradleIsApplicable";
		JobListener jobListener = new JobListener();
		Job.getJobManager().addJobChangeListener(jobListener);
		IProject project = Utils.createProject(projectName, "gradle");
		scanManager.scanAndUpdateResults(null, null, null, null);
		Job.getJobManager().join(CliJob.FAMILY, new NullProgressMonitor());
		assertJobInformation(projectName, jobListener);
		assertFalse(scanManager.getScanInProgress().get());
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
			if (event.getJob().belongsTo(CliJob.FAMILY)) {
				this.jobName = event.getJob().getName();
				numOfJobs.incrementAndGet();
			}
		}

		@Override
		public void done(IJobChangeEvent event) {
			if (event.getJob().belongsTo(CliJob.FAMILY)) {
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
