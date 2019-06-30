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
import com.jfrog.ide.common.filter.FilterManager;
import com.jfrog.ide.common.log.ProgressIndicator;
import com.jfrog.ide.common.scan.ComponentPrefix;
import com.jfrog.ide.common.scan.ScanManagerBase;
import com.jfrog.ide.eclipse.configuration.XrayServerConfigImpl;
import com.jfrog.ide.eclipse.log.Logger;
import com.jfrog.ide.eclipse.log.ProgressIndicatorImpl;
import com.jfrog.ide.eclipse.scheduling.ScanJob;
import com.jfrog.ide.eclipse.ui.FilterManagerSingleton;
import com.jfrog.ide.eclipse.ui.issues.IssuesTree;
import com.jfrog.ide.eclipse.ui.licenses.LicensesTree;
import com.jfrog.ide.eclipse.utils.ProjectsMap;
import com.jfrog.xray.client.services.summary.Components;

/**
 * @author yahavi
 */
public abstract class ScanManager extends ScanManagerBase {

	static final Path HOME_PATH = Paths.get(System.getProperty("user.home"), ".jfrog-eclipse-plugin");
	private IProgressMonitor monitor;
	IProject project;

	ScanManager(IProject project, ComponentPrefix prefix) throws IOException {
		super(HOME_PATH.resolve("cache"), project.getName(), Logger.getInstance(), XrayServerConfigImpl.getInstance(),
				prefix);
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

	public IProject getIProject() {
		return project;
	}
	
	public void setMonitor(IProgressMonitor monitor) {
		this.monitor = monitor;
	}

	/**
	 * Schedule a dependency scan.
	 * 
	 * @param quickScan - True iff this is a quick scan.
	 * @param parent    - The parent UI composite. Cancel the scan if the parent is
	 *                  disposed.
	 */
	public void scanAndUpdateResults(boolean quickScan, IssuesTree issuesTree, LicensesTree licensesTree, Composite parent) {
		ScanJob.doSchedule(project.getName(), new ScanRunnable(parent, issuesTree, licensesTree, quickScan));
	}

	/**
	 * Start a dependency scan.
	 */
	private class ScanRunnable implements ICoreRunnable {
		private LicensesTree licensesTree;
		private IssuesTree issuesTree;
		private boolean quickScan;
		private Composite parent;

		private ScanRunnable(Composite parent, IssuesTree issuesTree, LicensesTree licensesTree, boolean quickScan) {
			this.parent = parent;
			this.issuesTree = issuesTree;
			this.licensesTree = licensesTree;
			this.quickScan = quickScan;
		}

		@Override
		public void run(IProgressMonitor monitor) throws CoreException {
			ScanManager.this.monitor = monitor;
			if (isDisposed()) {
				return;
			}
			getLog().info("Performing scan for " + getProjectName());
			try {
				refreshDependencies(monitor);
				buildTree();
				if (isDisposed() || getScanResults() == null) {
					return;
				}
				ProgressIndicator indicator = new ProgressIndicatorImpl("Xray Scan - " + getProjectName(), monitor);
				scanAndCacheArtifacts(indicator, quickScan);
				addXrayInfoToTree(getScanResults());
				setScanResults();
			} catch (IOException e) {
				Logger.getInstance().error(e.getMessage(), e);
				return;
			}
		}

		private void setScanResults() {
			FilterManager filterManager = FilterManagerSingleton.getInstance();
			if (!getScanResults().isLeaf()) {
				addFilterMangerLicenses(filterManager);
			}
			DependenciesTree scanResults = getScanResults();
			issuesTree.addScanResults(getProjectName(), scanResults);
			licensesTree.addScanResults(getProjectName(), scanResults);
			if (isDisposed()) {
				return;
			}
			parent.getDisplay().syncExec(new Runnable() {
				@Override
				public void run() {
					if (monitor.isCanceled()) {
						return;
					}
					ProjectsMap.ProjectKey projectKey = ProjectsMap.createKey(getProjectName(),
							scanResults.getGeneralInfo());
					licensesTree.applyFilters(projectKey);
					issuesTree.applyFilters(projectKey);
				}
			});
		}
		
		private boolean isDisposed() {
			return parent == null || parent.isDisposed();
		}
	}

}
