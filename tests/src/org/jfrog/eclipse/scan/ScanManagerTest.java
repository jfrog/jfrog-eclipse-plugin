package org.jfrog.eclipse.scan;

import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.jfrog.eclipse.scheduling.ScanJob;
import org.jfrog.eclipse.test.utils.Utils;

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
}
