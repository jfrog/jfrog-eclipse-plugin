package com.jfrog.ide.eclipse.scan;

import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

import com.jfrog.ide.eclipse.utils.Utils;

import junit.framework.TestCase;

public class MavenScanManagerTest extends TestCase {

	public void testIsMavenApplicable() throws CoreException, IOException {
		IProject project = getMavenProject("mavenIsApplicable");
		assertTrue(MavenScanManager.isApplicable(project));
	}

	public void testIsMavenNotApplicable() throws CoreException, IOException {
		IProject project = getMavenProject("mavenIsNotApplicable");
		assertFalse(MavenScanManager.isApplicable(project));
	}
	
	private IProject getMavenProject(String projectLocation) throws IOException, CoreException {
		return Utils.createProject(projectLocation, "maven");
	}
}
