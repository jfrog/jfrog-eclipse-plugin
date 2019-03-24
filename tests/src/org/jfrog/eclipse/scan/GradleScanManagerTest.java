package org.jfrog.eclipse.scan;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.jfrog.eclipse.test.utils.Utils;
import org.jfrog.eclipse.utils.GradleArtifact;

import junit.framework.TestCase;

public class GradleScanManagerTest extends TestCase {

	public void testIsApplicable() throws CoreException, IOException {
		IProject project = getGradleProject("gradleIsApplicable");
		assertTrue(GradleScanManager.isApplicable(project));
	}

	public void testIsNotApplicable() throws CoreException, IOException {
		IProject project = getGradleProject("gradleIsNotApplicable");
		assertFalse(GradleScanManager.isApplicable(project));
	}

	public void testCreateGradleFile() throws IOException, CoreException {
		IProject project = getGradleProject("gradleIsApplicable");
		GradleScanManager gradleScanManager = new GradleScanManager(project);
		String gradleFileLocation = Utils.getGradleScriptFileLocation(gradleScanManager);
		File gradleScriptExpectedLocation = new File(System.getProperty("user.home") + File.separator + "jfrog-eclipse-plugin"
				+ File.separator + gradleScanManager.getVersion() + File.separator + gradleScanManager.getFileName());
		assertTrue(gradleScriptExpectedLocation.exists());
		assertEquals(gradleScriptExpectedLocation.getAbsolutePath(), gradleFileLocation);

	}
	
	public void testReadGeneratedJSonFile() throws IOException, CoreException {
		GradleScanManager gradleScanManager = generateDependenciesGraph("gradleIsApplicable");
		byte[] json = gradleScanManager.readGeneratedJson();
		byte[] result = Utils.getResultContent("generatedJson", "generatedGradleJson.txt");
		assertEquals(new String(result), new String(json));
	}

	public void testParseJsonResult() throws IOException, CoreException {
		GradleScanManager gradleScanManager = generateDependenciesGraph("gradleIsApplicable");
		gradleScanManager.parseJsonResult();
		GradleArtifact gradleArtifact = gradleScanManager.getGradleArtifact();
		assertEquals("gradleIsApplicable", gradleArtifact.artifactId);
		assertEquals("jfrog.org", gradleArtifact.groupId);
		assertEquals("1.0.0", gradleArtifact.version);
		assertEquals(1, gradleArtifact.getDependencies().length);
	}
	
	public void testParseNoDependenciesProject() throws IOException, CoreException {
		GradleScanManager gradleScanManager = generateDependenciesGraph("gradleNoDependencies");
		gradleScanManager.parseJsonResult();
		GradleArtifact gradleArtifact = gradleScanManager.getGradleArtifact();
		assertEquals("gradleNoDependencies", gradleArtifact.artifactId);
		assertEquals("jfrog.org", gradleArtifact.groupId);
		assertEquals("1.0.0", gradleArtifact.version);
		assertEquals(0, gradleArtifact.getDependencies().length);
	}

	
	public void testParseMultipleDependenciesProject() throws IOException, CoreException {
		GradleScanManager gradleScanManager = generateDependenciesGraph("gradleMultipleDependencies");
		gradleScanManager.parseJsonResult();
		GradleArtifact gradleArtifact = gradleScanManager.getGradleArtifact();
		assertEquals("gradleMultipleDependencies", gradleArtifact.artifactId);
		assertEquals("jfrog.org", gradleArtifact.groupId);
		assertEquals("1.0.0", gradleArtifact.version);
		assertEquals(6, gradleArtifact.getDependencies().length);
	}
	
	private GradleScanManager generateDependenciesGraph(String projectName) throws IOException, CoreException {
		IProject project = getGradleProject(projectName);
		GradleScanManager gradleScanManager = new GradleScanManager(project);
		String gradleFileLocation = Utils.getGradleScriptFileLocation(gradleScanManager);
		String rootProjectDir = project.getLocation().toPortableString();
		if (project.getLocation().toFile().isDirectory()) {
			rootProjectDir = project.getLocation().addTrailingSeparator().toPortableString();
		}
		gradleScanManager.generateDependenciesGraphAsJsonTask(rootProjectDir, gradleFileLocation);
		return gradleScanManager;
	}
	private IProject getGradleProject(String projectLocation) throws IOException, CoreException {
		return Utils.createProject(projectLocation, "gradle");
	}
}
