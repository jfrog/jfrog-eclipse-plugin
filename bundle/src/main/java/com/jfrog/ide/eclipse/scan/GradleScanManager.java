package com.jfrog.ide.eclipse.scan;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.buildship.core.GradleDistribution;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProgressEvent;
import org.gradle.tooling.ProgressListener;
import org.gradle.tooling.ProjectConnection;
import org.jfrog.build.extractor.scan.DependenciesTree;
import org.jfrog.build.extractor.scan.GeneralInfo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import com.jfrog.ide.common.scan.ComponentPrefix;
import com.jfrog.ide.eclipse.configuration.PreferenceConstants;
import com.jfrog.ide.eclipse.utils.GradleArtifact;

public class GradleScanManager extends ScanManager {

	private static final String TASK_NAME = "generateDependenciesGraphAsJson";
	public static final String GRADLE_INIT_SCRIPT = "dependencies.gradle";
	public static final String GRADLESCRIPTDIR = "gradleScript";

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
			return false;
		}
	}

	@Override
	void refreshDependencies(IProgressMonitor monitor) throws IOException {
		this.monitor = monitor;
		String rootProjectDir = project.getLocation().toPortableString();
		if (project.getLocation().toFile().isDirectory()) {
			rootProjectDir = project.getLocation().addTrailingSeparator().toPortableString();
		}

		String gradleFileNameFullPath = "/gradle/" + GRADLE_INIT_SCRIPT;
		ClassLoader classLoader = GradleScanManager.class.getClassLoader();
		// classLoader.getResourceAsStream(gradleFileNameFullPath) will work on all the
		// OSes
		try (InputStream res = classLoader.getResourceAsStream(gradleFileNameFullPath)) {
			String gradleFile = createGradleFile(res);
			if (StringUtils.isBlank(gradleFile)) {
				getLog().warn("Gradle init script wasn't created.");
				return;
			}
			generateDependenciesGraphAsJsonTask(rootProjectDir, gradleFile);
			parseJsonResult();
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

	public GradleArtifact getGradleArtifact() {
		return gradleArtifact;
	}

	private void removeDuplicateDependencies() {
		if (gradleArtifact != null && ArrayUtils.isNotEmpty(gradleArtifact.getDependencies())) {
			Set<GradleArtifact> dependenciesSet = Sets.newHashSet(gradleArtifact.getDependencies());
			gradleArtifact.setDependencies(dependenciesSet.toArray(new GradleArtifact[] {}));
		}
	}

	private String getComponentId(GradleArtifact gradleArtifact) {
		return gradleArtifact.getGroupId() + ":" + gradleArtifact.getArtifactId() + ":" + gradleArtifact.getVersion();
	}

	/**
	 * Populate root modules DependenciesTree with issues, licenses and general info
	 * from the scan cache.
	 */
	private void populateDependenciesTree(DependenciesTree scanTreeNode, GradleArtifact[] gradleArtifacts) {
		for (GradleArtifact artifact : gradleArtifacts) {
			String componentId = getComponentId(artifact);
			DependenciesTree child = new DependenciesTree(componentId);
			child.setGeneralInfo(new GeneralInfo(componentId, "", "", "Maven"));
			scanTreeNode.add(child);
			populateDependenciesTree(child, artifact.getDependencies());
		}
	}

	/**
	 * Create dependencies.gradle file for the project in the
	 * homeDir/.jfrog-eclipse-plugin/gradleScriptDir dir
	 * 
	 * @param in - File descriptor for the Gradle file.
	 * @return the Gradle file.
	 * @throws IOException in case of any IO failure.
	 */
	public String createGradleFile(InputStream in) throws IOException {
		Path gradleScriptDir = Files.createDirectories(HOME_PATH.resolve(GRADLESCRIPTDIR));
		Path gradleFile = gradleScriptDir.resolve(GRADLE_INIT_SCRIPT);
		Files.copy(in, gradleFile, StandardCopyOption.REPLACE_EXISTING);
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
		ProjectConnection connection = createGradleConnector().connect();
		try (OutputStream out = new FileOutputStream(Platform.getLogFileLocation().toOSString())) {
			getLog().info("Running the following command at " + project.getLocation().toString()
					+ ": gradle --init-script " + gradleFile + " " + TASK_NAME + " ");
			connection.newBuild().withArguments("--init-script", gradleFile).forTasks(TASK_NAME).setStandardOutput(out)
					.addProgressListener(new GradleProgressListener()).run();
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
		byte[] json = readGeneratedJson();
		if (json != null) {
			gradleArtifact = objectMapper.readValue(json, GradleArtifact.class);
			removeDuplicateDependencies();
		}
	}

	public byte[] readGeneratedJson() throws IOException {
		Path pathToTaskOutputDir = HOME_PATH.resolve(TASK_NAME).resolve(project.getName());
		if (!Files.exists(pathToTaskOutputDir)) {
			getLog().warn("Path is missing " + pathToTaskOutputDir.toAbsolutePath().toString());
			return null;
		}
		Path jsonOutputFile = pathToTaskOutputDir.resolve(getProjectName() + ".txt");
		return Files.readAllBytes(jsonOutputFile);
	}

	/**
	 * Create a Gradle connector according to the Gradle distribution chosen in
	 * 'Preferences' -> 'Gradle' -> 'Gradle distribution'.
	 *
	 * @return Gradle connector
	 */
	private GradleConnector createGradleConnector() {
		GradleConnector connector = GradleConnector.newConnector();
		connector.forProjectDirectory(new File(project.getLocation().toString()));
		IPreferencesService service = Platform.getPreferencesService();
		String gradleDistributionStr = service.getString(PreferenceConstants.GRADLE_PLUGIN_QUALIFIER,
				PreferenceConstants.GRADLE_DISTRIBUTION, "", null);
		try {
			GradleDistribution gradleDistribution = GradleDistribution.fromString(gradleDistributionStr);
			getLog().info("Gradle distribution type: " + gradleDistribution.getDisplayName());
			gradleDistribution.apply(connector);
		} catch (IllegalArgumentException exception) {
			getLog().info(
					"Couldn't find Gradle distribution type. Falling back to using Gradle wrapper, if it is configured as part of the project. If not, downloading Gradle. You can also configure Gradle distribution type in 'Preferences' -> 'Gradle' -> 'Gradle distribution'.");
		}

		return connector;
	}

	/**
	 * Log Gradle steps in the progress monitor.
	 */
	class GradleProgressListener implements ProgressListener {
		@Override
		public void statusChanged(ProgressEvent event) {
			if (monitor == null) {
				monitor = new NullProgressMonitor();
			}
			monitor.beginTask(event.getDescription(), IProgressMonitor.UNKNOWN);
		}
	}
}
