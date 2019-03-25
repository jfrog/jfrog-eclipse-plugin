package com.jfrog.ide.eclipse.scan;

import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.Status;

import com.jfrog.ide.eclipse.scheduling.ScanJob;
import com.jfrog.ide.eclipse.utils.Utils;

import junit.framework.TestCase;

public class ScanManagerTest extends TestCase {

	public void testSchedulingAJob() throws IOException, CoreException {
		String projectName = "gradleIsApplicable";
		IProject project = Utils.createProject(projectName, "gradle");
		ScanManager scanManager = new GradleScanManager(project);
		scanManager.scanAndUpdateResults(false, null, null, null);
		Job[] jobs = Job.getJobManager().find(ScanJob.FAMILY);
		assertEquals(1, jobs.length);
		assertEquals(projectName, jobs[0].getName());
	}	
	
	public void testScanFinished() throws IOException, CoreException {
		ScanManagersFactory.getInstance().getScanInProgress().set(true);
		String projectName = "gradleIsApplicable";
		IProject project = Utils.createProject(projectName, "gradle");
		ScanManager scanManager = new GradleScanManager(project);
		scanManager.scanAndUpdateResults(false, null, null, null);
		Job[] jobs = Job.getJobManager().find(ScanJob.FAMILY);
		assertEquals(1, jobs.length);
		assertEquals(projectName, jobs[0].getName());
		jobs[0].done(Status.OK_STATUS);
		assertFalse(ScanManagersFactory.getInstance().getScanInProgress().get());
	}

}
