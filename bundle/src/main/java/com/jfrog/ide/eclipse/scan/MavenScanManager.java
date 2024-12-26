package com.jfrog.ide.eclipse.scan;

import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectChangedListener;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.MavenProjectChangedEvent;
import org.eclipse.swt.widgets.Composite;
import org.jfrog.build.extractor.scan.DependencyTree;
import org.jfrog.build.extractor.scan.GeneralInfo;
import org.jfrog.build.extractor.scan.Scope;

import com.google.common.collect.Sets;
import com.jfrog.ide.common.scan.ComponentPrefix;
import com.jfrog.ide.eclipse.log.Logger;
import com.jfrog.ide.eclipse.scheduling.ScanJob;
import com.jfrog.ide.eclipse.ui.issues.IssuesTree;
import com.jfrog.ide.eclipse.ui.licenses.LicensesTree;

/**
 * @author yahavi
 */
public class MavenScanManager extends ScanManager {

	private DependencyNode mavenDependenciesRoot;
	private MavenProject mavenProject;
	private Composite parent;

	public MavenScanManager(IProject project, Composite parent) throws IOException {
		super(project, ComponentPrefix.GAV);
		getLog().info("Found Maven project: " + getProjectName());
		this.parent = parent;
		
	}

	public static boolean isApplicable(IProject project) {
		try {
			return project.hasNature("org.eclipse.m2e.core.maven2Nature");
		} catch (CoreException ce) {
			return false;
		}
	}

	void refreshDependencies(IProgressMonitor monitor) throws CoreException {
		IMavenProjectFacade facade = MavenPlugin.getMavenProjectRegistry().getProject(project);
		if (facade == null) {
			// If workspace is not ready yet, the get project will return null.
			// Adding a listener to wait for workspace build completion.
			MavenPlugin.getMavenProjectRegistry().addMavenProjectChangedListener(new MavenProjectListener());
			return;
		}
		mavenProject = facade.getMavenProject(monitor);
		mavenDependenciesRoot = MavenPlugin.getMavenModelManager().readDependencyTree(facade, mavenProject,
				Artifact.SCOPE_COMPILE_PLUS_RUNTIME, monitor);
	}

	@Override
	void buildTree() throws CoreException {
		refreshDependencies(getMonitor());
		if (mavenProject == null) {
			return;
		}
		DependencyTree rootNode = new DependencyTree(mavenProject.getName()); 
		populateScanTreeNode(rootNode, mavenDependenciesRoot);
		GeneralInfo generalInfo = new GeneralInfo().groupId(mavenProject.getGroupId())
				.artifactId(mavenProject.getArtifactId()).version(mavenProject.getVersion());
		rootNode.setGeneralInfo(generalInfo);
		setScanResults(rootNode); 
	}

	/**
	 * Populate root modules DependencyTree with issues, licenses and general info
	 * from the scan cache.
	 */
	private void populateScanTreeNode(DependencyTree scanTreeNode, DependencyNode dependencyNode) {
		dependencyNode.getChildren().forEach(dependencyChild -> {
			String componentId = getComponentId(dependencyChild);
			DependencyTree child = new DependencyTree(componentId);
			String componentName = dependencyChild.getArtifact().getArtifactId();
			child.setGeneralInfo(new GeneralInfo(componentId, componentName, "", "Maven"));
			// set dependency scope
			String componentScope = dependencyChild.getDependency().getScope();
			child.setScopes(Sets.newHashSet(new Scope(componentScope)));
			
			scanTreeNode.add(child);
			populateScanTreeNode(child, dependencyChild);
		});
	}

	private String getComponentId(DependencyNode dependencyNode) {
		org.eclipse.aether.artifact.Artifact artifact = dependencyNode.getArtifact();
		return artifact.getGroupId() + ":" + artifact.getArtifactId() + ":" + artifact.getBaseVersion();
	}

	/**
	 * Implements listener that waits for Maven jobs completion.
	 */
	private class MavenProjectListener implements IMavenProjectChangedListener {
		@Override
		public void mavenProjectChanged(MavenProjectChangedEvent[] events, IProgressMonitor monitor) {
			try {
				Job[] jobs = Job.getJobManager().find(ScanJob.FAMILY);
				if (jobs != null) {
					boolean alreadyRun = Arrays.stream(jobs)
							.anyMatch(job -> StringUtils.equals(job.getName(), getProjectName()));
					if (alreadyRun) {
						Logger.getInstance().info("Found existing job: " + getProjectName());
						return;
					}
				}
				IssuesTree issuesTree = IssuesTree.getInstance();
				LicensesTree licensesTree = LicensesTree.getInstance();
				if (issuesTree != null && licensesTree != null) {
					scanAndUpdateResults(false, issuesTree, licensesTree, parent);
				}
			} finally {
				MavenPlugin.getMavenProjectRegistry().removeMavenProjectChangedListener(this);
			}
		}
	}
}
