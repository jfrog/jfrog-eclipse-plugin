package com.jfrog.ide.eclipse.scan;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CancellationException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Composite;
import org.jfrog.build.extractor.scan.DependenciesTree;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jfrog.ide.common.log.ProgressIndicator;
import com.jfrog.ide.common.scan.ComponentPrefix;
import com.jfrog.ide.common.scan.ScanManagerBase;
import com.jfrog.ide.eclipse.configuration.XrayServerConfigImpl;
import com.jfrog.ide.eclipse.log.Logger;
import com.jfrog.ide.eclipse.log.ProgressIndicatorImpl;
import com.jfrog.ide.eclipse.scheduling.ScanJob;
import com.jfrog.ide.eclipse.ui.ProjectsMap;
import com.jfrog.ide.eclipse.ui.issues.IssuesTree;
import com.jfrog.ide.eclipse.ui.licenses.LicensesTree;
import com.jfrog.xray.client.services.summary.Components;

/**
 * @author yahavi
 */
public abstract class ScanManager extends ScanManagerBase {

	static final Path HOME_PATH = Paths.get(System.getProperty("user.home"), "jfrog-eclipse-plugin");
	private IProgressMonitor monitor;
	IProject project;

	ScanManager(IProject project, ComponentPrefix prefix) throws IOException {
		super(HOME_PATH.resolve("cache"), project.getName(), Logger.getLogger(), XrayServerConfigImpl.getInstance(), prefix);
		this.project = project;
		Files.createDirectories(HOME_PATH);
	}

	/**
	 * Refresh project dependencies.
	 * 
	 * @throws IOException
	 * @throws CoreException
	 */
	abstract void refreshDependencies(IProgressMonitor monitor) throws IOException, CoreException;

	/**
	 * Collect and return {@link Components} to be scanned by JFrog Xray.
	 * Implementation should be project type specific.
	 * 
	 * @return
	 * 
	 * @throws CoreException
	 * @throws IOException
	 * @throws JsonProcessingException
	 */
	abstract void buildTree() throws CoreException, JsonProcessingException, IOException;

	@Override
	public void checkCanceled() {
		if (monitor != null && monitor.isCanceled()) {
			throw new CancellationException("Xray scan was canceled");
		}
	}

	/**
	 * Launch dependency scan.
	 * 
	 * @param event
	 * 
	 * @throws CoreException
	 */
	public void scanAndUpdateResults(boolean quickScan, IssuesTree issuesTree, LicensesTree licensesTree,
			Composite parent) {
		ScanJob.doSchedule(project.getName(), new ICoreRunnable() {
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				ScanManager.this.monitor = monitor;
				if (parent == null || parent.isDisposed()) {
					return;
				}
				getLog().info("Performing scan for " + getProjectName());
				try {
					refreshDependencies(monitor);
					buildTree();
				} catch (IOException e) {
					Logger.getLogger().error(e.getMessage(), e);
					return;
				}
				if (parent == null || parent.isDisposed() || getScanResults() == null) {
					return;
				}
				ProgressIndicator indicator = new ProgressIndicatorImpl("Xray Scan - " + getProjectName(), monitor);
				scanAndCacheArtifacts(indicator, quickScan);
				addXrayInfoToTree(getScanResults());
				if (!getScanResults().isLeaf()) {
					addFilterMangerLicenses();
				}
				DependenciesTree scanResults = getScanResults();
				issuesTree.addScanResults(getProjectName(), scanResults);
				licensesTree.addScanResults(getProjectName(), scanResults);
				if (parent == null || parent.isDisposed()) {
					return;
				}
				parent.getDisplay().syncExec(new Runnable() {
					public void run() {
						if (monitor.isCanceled()) {
							return;
						}
						ProjectsMap.ProjectKey projectKey = ProjectsMap.createKey(getProjectName(), scanResults.getGeneralInfo());
						licensesTree.applyFilters(projectKey);
						issuesTree.applyFilters(projectKey);	
					}
				});
			}
		});
	}

	public IProject getIProject() {
		return project;
	}
}
