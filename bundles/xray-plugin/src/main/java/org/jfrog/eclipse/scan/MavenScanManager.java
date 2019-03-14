package org.jfrog.eclipse.scan;

import java.io.IOException;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.jfrog.build.extractor.scan.DependenciesTree;
import org.jfrog.build.extractor.scan.GeneralInfo;
import org.jfrog.scan.ComponentPrefix;

/**
 * @author yahavi
 */
public class MavenScanManager extends ScanManager {

	private DependencyNode mavenDependenciesRoot;
	private MavenProject mavenProject;

	public MavenScanManager(IProject project) throws IOException {
		super(project, ComponentPrefix.GAV);
		getLog().info("Found Maven project: " + getProjectName());
	}

	public static boolean isApplicable(IProject project) {
		try {
			return project.getNature("org.eclipse.m2e.core.maven2Nature") != null
					&& MavenPlugin.getMavenProjectRegistry().getProject(project) != null;
		} catch (CoreException ce) {
			// Ignore
		}
		return false;
	}

	@Override
	void refreshDependencies() throws CoreException {
		IMavenProjectFacade facade = MavenPlugin.getMavenProjectRegistry().getProject(project);
		mavenProject = facade.getMavenProject(new NullProgressMonitor());
		mavenDependenciesRoot = MavenPlugin.getMavenModelManager().readDependencyTree(facade, mavenProject,
				Artifact.SCOPE_COMPILE_PLUS_RUNTIME, new NullProgressMonitor());
	}

	@Override
	void buildTree() throws CoreException {
		DependenciesTree rootNode = new DependenciesTree(mavenProject.getName());
		GeneralInfo generalInfo = new GeneralInfo().groupId(mavenProject.getGroupId())
				.artifactId(mavenProject.getArtifactId()).version(mavenProject.getVersion());
		rootNode.setGeneralInfo(generalInfo);
		populateScanTreeNode(rootNode, mavenDependenciesRoot);
		setScanResults(rootNode);
	}

	/**
	 * Populate root modules ScanTreeNode with issues, licenses and general info
	 * from the scan cache.
	 */
	private void populateScanTreeNode(DependenciesTree scanTreeNode, DependencyNode dependencyNode) {
		dependencyNode.getChildren().forEach(dependencyChild -> {
			String componentId = getComponentId(dependencyChild);
			DependenciesTree child = new DependenciesTree(componentId);
			scanTreeNode.add(child);
			populateScanTreeNode(child, dependencyChild);
		});
	}

	private String getComponentId(DependencyNode dependencyNode) {
		org.eclipse.aether.artifact.Artifact artifact = dependencyNode.getArtifact();
		return artifact.getGroupId() + ":" + artifact.getArtifactId() + ":" + artifact.getBaseVersion();
	}

}
