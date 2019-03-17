package org.jfrog.eclipse.scan;

import java.io.IOException;
import java.util.concurrent.CancellationException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Composite;
import org.jfrog.build.extractor.scan.DependenciesTree;
import org.jfrog.eclipse.configuration.XrayServerConfigImpl;
import org.jfrog.eclipse.log.Logger;
import org.jfrog.eclipse.log.ProgressIndicatorImpl;
import org.jfrog.eclipse.scheduling.ScanJob;
import org.jfrog.eclipse.ui.issues.IssuesTree;
import org.jfrog.eclipse.ui.licenses.LicensesTree;
import org.jfrog.log.ProgressIndicator;
import org.jfrog.scan.ComponentPrefix;
import org.jfrog.scan.ScanManagerBase;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jfrog.xray.client.services.summary.Components;

/**
 * @author yahavi
 */
public abstract class ScanManager extends ScanManagerBase {

	private IProgressMonitor monitor;
	IProject project;

	ScanManager(IProject project, ComponentPrefix prefix) throws IOException {
		super(project.getName(), Logger.getLogger(), XrayServerConfigImpl.getInstance(), prefix);
		this.project = project;
	}

	/**
	 * Refresh project dependencies.
	 * 
	 * @throws IOException
	 * @throws CoreException
	 */
	abstract void refreshDependencies() throws IOException, CoreException;

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
				if (parent.isDisposed()) {
					return;
				}
				ProgressIndicator indicator = new ProgressIndicatorImpl("Xray Scan - " + getProjectName(), monitor);
				getLog().info("Performing scan for " + getProjectName());
				try {
					refreshDependencies();
					buildTree();
				} catch (IOException e) {
					Logger.getLogger().error(e.getMessage(), e);
				}
				if (parent.isDisposed()) {
					return;
				}
				if (getScanResults() == null) {
					return;
				}
				scanAndCacheArtifacts(indicator, quickScan);
				addXrayInfoToTree(getScanResults());
				if (!getScanResults().isLeaf()) {
					setUiLicenses();
				}
				DependenciesTree scanResults = getScanResults();
				issuesTree.addScanResults(scanResults, getProjectName());
				licensesTree.addScanResults(scanResults, getProjectName());
				if (parent.isDisposed()) {
					return;
				}
				parent.getDisplay().syncExec(new Runnable() {
					public void run() {
						licensesTree.applyFilters(getProjectName());
						issuesTree.applyFilters(getProjectName());
					}
				});

			}
		});
	}
	
	public IProject getIProject() {
		return project;
	}
}
