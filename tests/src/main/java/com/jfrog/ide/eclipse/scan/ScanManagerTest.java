package com.jfrog.ide.eclipse.scan;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
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

import com.jfrog.ide.eclipse.scheduling.CliJob;
import com.jfrog.ide.eclipse.ui.issues.IssuesTree;
import com.jfrog.ide.common.nodes.FileTreeNode;

import com.jfrog.ide.eclipse.utils.Utils;
import junit.framework.TestCase;

public class ScanManagerTest extends TestCase {
	private ScanManager scanManager = ScanManager.getInstance();
	private ScanCache scanCache = ScanCache.getInstance();
	private IssuesTree issuesTree = IssuesTree.getInstance();

	// TODO: generate test for scanning: Maven, Gradle and NPM projects. 

	public void testScanMavenProject() throws IOException, CoreException, InterruptedException {
		String projectName = "mavenIsApplicable";
		CountDownLatch latch = new CountDownLatch(1);
		
		JobListener jobListener = new JobListener(latch);
		IProject project = Utils.createProject(projectName, "maven");
		
		Job.getJobManager().addJobChangeListener(jobListener);
		
		scanManager.scanAndUpdateResults(issuesTree, null, project, null);
		
		// wait for scanAndUpdateResults to return, then check issuesTree has results 
		 boolean completed = latch.await(60, java.util.concurrent.TimeUnit.SECONDS);
		
        // Ensure the job completed
        assertTrue(completed);

        // Check the issuesTree has results
//        assertTrue(scanCache.getScanResults().size() > 0);
        cleanup(jobListener);
	}
	
	public void testSchedulingAJob()
			throws IOException, CoreException, OperationCanceledException, InterruptedException {
		String projectName = "gradleIsApplicable";
		JobListener jobListener = new JobListener(null);
		IProject project = Utils.createProject(projectName, "gradle");
		Job.getJobManager().addJobChangeListener(jobListener);
		scanManager.scanAndUpdateResults(null, null, project, null);
		Job.getJobManager().join(CliJob.FAMILY, new NullProgressMonitor());
		assertJobInformation(projectName, jobListener);
		cleanup(jobListener);
	}

	public void testScanFinished() throws IOException, CoreException, OperationCanceledException, InterruptedException {
		scanManager.getScanInProgress().set(true);
		String projectName = "gradleIsApplicable";
		JobListener jobListener = new JobListener(null);
		Job.getJobManager().addJobChangeListener(jobListener);
		IProject project = Utils.createProject(projectName, "gradle");
		scanManager.scanAndUpdateResults(null, null, project, null);
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
		assertEquals("Performing Scan: " + projectName, jobListener.getJobName());
		assertEquals(1, jobListener.numOfJobs());
	}

	static class JobListener extends JobChangeAdapter {

		private static AtomicBoolean jobExists = new AtomicBoolean(false);
		private String jobName;
		private static AtomicInteger numOfJobs = new AtomicInteger();
		private final CountDownLatch latch;

        public JobListener(CountDownLatch latch) {
            this.latch = latch;
        }
		
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
			if (latch != null) {
				latch.countDown();
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
