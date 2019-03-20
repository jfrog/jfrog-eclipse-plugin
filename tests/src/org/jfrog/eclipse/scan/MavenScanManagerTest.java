package org.jfrog.eclipse.scan;

import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.jfrog.eclipse.test.utils.Utils;

import junit.framework.TestCase;

public class MavenScanManagerTest extends TestCase {

	public void testIsMavenApplicable() throws CoreException, IOException {
		String projectLocation = "mavenIsApplicable";
		IProject project = Utils.createProject(projectLocation, "maven");
		assertTrue(MavenScanManager.isApplicable(project));
	}

	public void testIsMavenNotApplicable() throws CoreException, IOException {
		String projectLocation = "mavenIsNotApplicable";
		IProject project = Utils.createProject(projectLocation, "maven");
		assertFalse(MavenScanManager.isApplicable(project));
	}
}
