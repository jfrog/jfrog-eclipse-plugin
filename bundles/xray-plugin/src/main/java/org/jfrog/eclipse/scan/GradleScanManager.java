package org.jfrog.eclipse.scan;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.LinkedHashSet;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.jfrog.build.extractor.scan.DependenciesTree;
import org.jfrog.build.extractor.scan.GeneralInfo;
import org.jfrog.eclipse.log.Logger;
import org.jfrog.eclipse.utils.GradleArtifact;
import org.jfrog.scan.ComponentPrefix;

import com.fasterxml.jackson.databind.ObjectMapper;

public class GradleScanManager extends ScanManager {

	private static final String TASK_NAME = "generateDependenciesGraphAsJson";
	private static final String GRADLE_FILE_NAME = "dependencies.gradle";
	private GradleArtifact gradleArtifact;

	public GradleScanManager(IProject project) throws IOException {
		super(project, ComponentPrefix.GAV);
		getLog().info("Found Gradle project: " + getProjectName());
	}

	public static boolean isApplicable(IProject project) {
		try {
			return project.hasNature("org.eclipse.buildship.core.gradleprojectnature");
		} catch (CoreException ce) {
			// Ignore
		}
		return false;
	}

	@Override
	void refreshDependencies() throws IOException {
		String rootProjectDir = project.getLocation().toPortableString();
		if (project.getLocation().toFile().isDirectory()) {
			rootProjectDir = project.getLocation().addTrailingSeparator().toPortableString();
		}

		String gradleFileNameFullPath = "/gradle/" + GRADLE_FILE_NAME;
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		try (InputStream res = classLoader.getResourceAsStream(gradleFileNameFullPath)) {
			String gradleFile = createGradleFile(GRADLE_FILE_NAME, res);
			if (gradleFile == null || gradleFile.isEmpty()) {
				getLog().warn("Gradle File wasn't created.");
				return;
			}
			runGenerateDependenciesGraphAsJsonTask(rootProjectDir, gradleFile);
			// Read the files and convert from json
			parseJsonResult();
			// clean duplicates
			removeDuplicateDependencies();
		}

	}

	@Override
	void buildTree() {
		DependenciesTree rootNode = new DependenciesTree(getProjectName());
		GeneralInfo generalInfo = new GeneralInfo();
		generalInfo.groupId(gradleArtifact.getGroupId()).artifactId(gradleArtifact.getArtifactId())
				.version(gradleArtifact.getVersion());
		rootNode.setGeneralInfo(generalInfo);
		GradleArtifact[] dependencies = gradleArtifact.getDependencies();
		if (ArrayUtils.isNotEmpty(dependencies)) {
			populateScanTreeNode(rootNode, dependencies);
		}
		setScanResults(rootNode);
	}

	private void removeDuplicateDependencies() {
		if (gradleArtifact.getDependencies() != null && gradleArtifact.getDependencies().length > 0) {
			// Create set from array elements
			LinkedHashSet<GradleArtifact> dependencies = new LinkedHashSet<>(
					Arrays.asList(gradleArtifact.getDependencies()));
			// Get back the array without duplicates
			gradleArtifact.setDependencies(dependencies.toArray(new GradleArtifact[] {}));
		}
	}

	private String getComponentId(GradleArtifact gradleArtifact) {
		return gradleArtifact.getGroupId() + ":" + gradleArtifact.getArtifactId() + ":" + gradleArtifact.getVersion();
	}

	private void populateScanTreeNode(DependenciesTree scanTreeNode, GradleArtifact[] gradleArtifacts) {
		for (GradleArtifact artifact : gradleArtifacts) {
			String componentId = getComponentId(artifact);
			DependenciesTree child = new DependenciesTree(componentId);
			scanTreeNode.add(child);
			populateScanTreeNode(child, artifact.getDependencies());
		}
	}

	public String createGradleFile(String gradleFileName, InputStream in) throws IOException {
		File homeDir = new File(System.getProperty("user.home") + File.separator + "jfrog-eclipse-plugin");
		if (!homeDir.exists()) {
			homeDir.mkdirs();
		}
		File versionDir = new File(homeDir.getAbsoluteFile() + File.separator + "01");
		if (!versionDir.exists()) {
			versionDir.mkdir();
		}
		File gradleFile = new File(versionDir.getAbsoluteFile() + File.separator + gradleFileName);
		if (!gradleFile.exists()) {
			Files.copy(in, gradleFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
		return gradleFile.getAbsolutePath();
	}

	public void runGenerateDependenciesGraphAsJsonTask(String rootProjectDir, String gradleFile) {
		GradleConnector connector = GradleConnector.newConnector();
		connector.forProjectDirectory(new File(project.getLocation().toString()));
		ProjectConnection connection = connector.connect();
		try (OutputStream out = new FileOutputStream(Platform.getLogFileLocation().toOSString())) {
			getLog().info("Running the following command at " + project.getLocation().toString()
					+ ": gradle --init-script " + gradleFile + " " + TASK_NAME + " ");
			connection.newBuild().withArguments("--init-script", gradleFile).forTasks(TASK_NAME).setStandardOutput(out)
					.run();
			// To write the output: .setStandardOutput(System.out)
		} catch (RuntimeException re) {
			getLog().error("Gradle run finished with the following error: " + re.getCause());
			getLog().error("", re);
		} catch (IOException ioe) {
			getLog().error(ioe.getCause().getMessage());
			getLog().error("", ioe);
		} finally {
			connection.close();
		}
	}

	public void parseJsonResult() throws IOException {
		File homeDir = new File(System.getProperty("user.home") + File.separator + "jfrog-eclipse-plugin");
		File pathToTaskOutputDir = new File(
				homeDir.getAbsolutePath() + File.separator + TASK_NAME + File.separator + project.getName());
		if (!pathToTaskOutputDir.exists()) {
			getLog().warn("Path is missing " + pathToTaskOutputDir.getAbsolutePath());
			return;
		}
		File jsonOutputFile = new File(pathToTaskOutputDir + File.separator + getProjectName() + ".txt");
		BufferedReader reader = new BufferedReader(new FileReader(jsonOutputFile));
		StringBuilder json = new StringBuilder();
		String line = reader.readLine();
		while (line != null) {
			json.append(line);
			line = reader.readLine();
		}
		reader.close();
		ObjectMapper objectMapper = new ObjectMapper();
		gradleArtifact = objectMapper.readValue(json.toString(), GradleArtifact.class);
	}
}
