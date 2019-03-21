package org.jfrog.eclipse.scan;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProgressEvent;
import org.gradle.tooling.ProgressListener;
import org.gradle.tooling.ProjectConnection;
import org.jfrog.build.extractor.scan.DependenciesTree;
import org.jfrog.build.extractor.scan.GeneralInfo;
import org.jfrog.eclipse.utils.GradleArtifact;
import org.jfrog.scan.ComponentPrefix;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;

public class GradleScanManager extends ScanManager {

	private static final String TASK_NAME = "generateDependenciesGraphAsJson";
	private static final String GRADLE_FILE_NAME = "dependencies.gradle";
	private static final String VERSION = "01";

	private static ObjectMapper objectMapper = new ObjectMapper();
	private GradleArtifact gradleArtifact;
	private IProgressMonitor monitor;

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
	void refreshDependencies(IProgressMonitor monitor) throws IOException {
		this.monitor = monitor;
		String rootProjectDir = project.getLocation().toPortableString();
		if (project.getLocation().toFile().isDirectory()) {
			rootProjectDir = project.getLocation().addTrailingSeparator().toPortableString();
		}

		String gradleFileNameFullPath = "/gradle/" + GRADLE_FILE_NAME;
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		try (InputStream res = classLoader.getResourceAsStream(gradleFileNameFullPath)) {
			String gradleFile = createGradleFile(res);
			if (StringUtils.isBlank(gradleFile)) {
				getLog().warn("Gradle File wasn't created.");
				return;
			}
			generateDependenciesGraphAsJsonTask(rootProjectDir, gradleFile);
			parseJsonResult();
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
			populateDependenciesTree(rootNode, dependencies);
		}
		setScanResults(rootNode);
	}

	private void removeDuplicateDependencies() {
		if (ArrayUtils.isNotEmpty(gradleArtifact.getDependencies())) {
			Set<GradleArtifact> dependenciesSet = Sets.newHashSet(gradleArtifact.getDependencies());
			gradleArtifact.setDependencies(dependenciesSet.toArray(new GradleArtifact[] {}));
		}
	}

	private String getComponentId(GradleArtifact gradleArtifact) {
		return gradleArtifact.getGroupId() + ":" + gradleArtifact.getArtifactId() + ":" + gradleArtifact.getVersion();
	}

	private void populateDependenciesTree(DependenciesTree scanTreeNode, GradleArtifact[] gradleArtifacts) {
		for (GradleArtifact artifact : gradleArtifacts) {
			String componentId = getComponentId(artifact);
			DependenciesTree child = new DependenciesTree(componentId);
			scanTreeNode.add(child);
			populateDependenciesTree(child, artifact.getDependencies());
		}
	}

	/**
	 * Create dependencies.gradle file for the project if it doesn't exist.
	 * 
	 * @param in - File descriptor for the Gradle file.
	 * @return the Gradle file.
	 * @throws IOException in case of any IO failure.
	 */
	public String createGradleFile(InputStream in) throws IOException {
		Path versionDir = Files.createDirectories(HOME_PATH.resolve(VERSION));
		Path gradleFile = versionDir.resolve(GRADLE_FILE_NAME);
		if (!Files.exists(gradleFile)) {
			Files.copy(in, gradleFile);
		}
		return gradleFile.toAbsolutePath().toString();
	}

	/**
	 * Run 'gradle --init-script' that generates the dependencies graph.
	 * 
	 * @param rootProjectDir - The root project directory.
	 * @param gradleFile     - Path to the 'dependencies.gradle' file.
	 * @throws IOException in case of any IO failures in the eclipse logs.
	 */
	public void generateDependenciesGraphAsJsonTask(String rootProjectDir, String gradleFile) throws IOException {
		GradleConnector connector = GradleConnector.newConnector();
		connector.forProjectDirectory(new File(project.getLocation().toString()));
		ProjectConnection connection = connector.connect();
		try (OutputStream out = new FileOutputStream(Platform.getLogFileLocation().toOSString())) {
			getLog().info("Running the following command at " + project.getLocation().toString()
					+ ": gradle --init-script " + gradleFile + " " + TASK_NAME + " ");
			connection.newBuild().withArguments("--init-script", gradleFile).forTasks(TASK_NAME).setStandardOutput(out)
					.addProgressListener(new GradleProgressListener()).run();
		} catch (RuntimeException re) {
			getLog().error("Gradle run finished with the following error: " + re.getCause(), re);
		} finally {
			connection.close();
		}
	}

	/**
	 * Read the files and convert from JSON.
	 * 
	 * @throws IOException in case of incorrect JSON file.
	 */
	public void parseJsonResult() throws IOException {
		Path pathToTaskOutputDir = HOME_PATH.resolve(TASK_NAME).resolve(project.getName());
		if (!Files.exists(pathToTaskOutputDir)) {
			getLog().warn("Path is missing " + pathToTaskOutputDir.toAbsolutePath().toString());
			return;
		}
		Path jsonOutputFile = pathToTaskOutputDir.resolve(getProjectName() + ".txt");
		byte[] json = Files.readAllBytes(jsonOutputFile);
		gradleArtifact = objectMapper.readValue(json, GradleArtifact.class);
	}

	/**
	 * Log Gradle steps in the progress monitor.
	 */
	class GradleProgressListener implements ProgressListener {
		@Override
		public void statusChanged(ProgressEvent event) {
			monitor.beginTask(event.getDescription(), IProgressMonitor.UNKNOWN);
		}
	}
	
	/**
	 * 
	 * @return the version
	 */
	public String getVersion() {
		return VERSION;
	}
	
	/**
	 * @return the Gradle file name
	 */
	public String getFileName() {
		return GRADLE_FILE_NAME;
	}
}
