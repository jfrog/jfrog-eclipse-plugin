package org.jfrog.eclipse.scan;

import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.jfrog.eclipse.test.utils.Utils;

import junit.framework.TestCase;

public class GradleScanManagerTest extends TestCase {

	public void testIsApplicable() throws CoreException, IOException {
		String projectName = "gradleIsApplicable";
		IProject project = Utils.createProject(projectName);
		assertTrue(GradleScanManager.isApplicable(project));
	}
	
	public void testIsNotApplicable() throws CoreException, IOException {
		String projectName = "gradleIsNotApplicable";
		IProject project = Utils.createProject(projectName);
		assertFalse(GradleScanManager.isApplicable(project));
	}
}
