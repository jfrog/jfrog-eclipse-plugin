package org.jfrog.eclipse.scan;

import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.jfrog.eclipse.test.utils.Utils;

import junit.framework.TestCase;

public class GradleScanManagerTest extends TestCase {

	public void testIsApplicable() throws CoreException, IOException {
		String projectLocation = "gradleIsApplicable";
		IProject project = Utils.createProject(projectLocation, "gradle");
		assertTrue(GradleScanManager.isApplicable(project));
	}

	public void testIsNotApplicable() throws CoreException, IOException {
		String projectLocation = "gradleIsNotApplicable";
		IProject project = Utils.createProject(projectLocation, "gradle");
		assertFalse(GradleScanManager.isApplicable(project));
	}

	public void testCreateGradleFile() throws IOException, CoreException {
		String projectLocation = "gradleIsApplicable";
		IProject project = Utils.createProject(projectLocation, "gradle");
		GradleScanManager gradleScanManager = new GradleScanManager(project);
		Utils.assertGradleFileCreation(gradleScanManager);
	}
}
