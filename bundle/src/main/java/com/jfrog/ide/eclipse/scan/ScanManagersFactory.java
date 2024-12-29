package com.jfrog.ide.eclipse.scan;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Composite;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.jfrog.ide.common.utils.PackageFileFinder;
import com.jfrog.ide.eclipse.log.Logger;
import com.jfrog.ide.eclipse.npm.NpmProject;
import com.jfrog.ide.eclipse.scheduling.ScanJob;
import com.jfrog.ide.eclipse.ui.issues.ComponentIssueDetails;
import com.jfrog.ide.eclipse.ui.issues.IssuesTree;
import com.jfrog.ide.eclipse.ui.licenses.ComponentLicenseDetails;
import com.jfrog.ide.eclipse.ui.licenses.LicensesTree;
import com.jfrog.ide.eclipse.configuration.PreferenceConstants;

/**
 * @author yahavi
 */
public class ScanManagersFactory {

	private static ScanManagersFactory instance;
	private List<ScanManager> scanManagers = Lists.newArrayList();

	// Lock to prevent multiple simultaneous scans
	private AtomicBoolean scanInProgress = new AtomicBoolean(false);

	private ScanManagersFactory() {
	}

	public static ScanManagersFactory getInstance() {
		if (instance == null) {
			instance = new ScanManagersFactory();
		}
		return instance;
	}

	public Collection<ScanManager> getScanManagers() {
		return scanManagers;
	}

	/**
	 * Start an Xray scan.
	 * 
	 * @param parent    - The parent composite requested to perform the scan. Will
	 *                  be used later to check if it's disposed.
	 * @param quickScan - True to use scan cache in case they are not invalidated.
	 */
	public void startScan(Composite parent, boolean quickScan) {
		if (isScanInProgress()) {
			Logger.getInstance().info("Previous scan still running...");
			return;
		}
		refreshScanManagers(parent);

		// Cancel other jobs
		Job[] jobs = Job.getJobManager().find(ScanJob.FAMILY);
		if (ArrayUtils.isNotEmpty(jobs)) {
			for (Job job : jobs) {
				Logger.getInstance().info("Cancling previous running scan: " + job.getName());
				job.cancel();
			}
		}

		if (!scanManagers.isEmpty()) {
			scanInProgress.compareAndSet(false, true);
		}
		IssuesTree issuesTree = IssuesTree.getInstance();
		LicensesTree licensesTree = LicensesTree.getInstance();
		if (issuesTree == null || licensesTree == null) {
			return;
		}
		resetViews(issuesTree, licensesTree);
		for (ScanManager scanManager : getScanManagers()) {
			scanManager.scanAndUpdateResults(quickScan, issuesTree, licensesTree, parent);
		}
	}

	/**
	 * Initialize scan managers list.
	 * 
	 * @param parent - The parent composite requested to perform the scan. Will be
	 *               used later to check if it's disposed.
	 */
	public void refreshScanManagers(Composite parent) {
		scanManagers = Lists.newArrayList();
		IWorkspace iworkspace = ResourcesPlugin.getWorkspace();
		IProject[] projects = iworkspace.getRoot().getProjects();
		if (projects.length > 0) {
			try {
				Set<Path> paths = Sets.newHashSet();
				
				// refresh Maven and Gradle managers
				for (IProject project : projects) {
					if (!project.isOpen()) {
						Logger.getInstance().info("Project is closed: " + project.getName());
						continue;
					}
					if (MavenScanManager.isApplicable(project)) {
						scanManagers.add(new MavenScanManager(project, parent));
					}
					if (GradleScanManager.isApplicable(project)) {
						scanManagers.add(new GradleScanManager(project));
					}
					paths.add(project.getLocation().toFile().toPath());
				}
				
				// refresh Npm manager
				PackageFileFinder packageFileFinder = new PackageFileFinder(paths, "", Logger.getInstance());
				Set<String> packageJsonDirs = packageFileFinder.getNpmPackagesFilePairs();
				for (String dir : packageJsonDirs) {
					IProject npmProject = new NpmProject(dir, iworkspace);
					scanManagers.add(new NpmScanManager(npmProject));
				}
			} catch (IOException e) {
				Logger.getInstance().error(e.getMessage(), e);
			}
		}
	}

	public boolean isScanInProgress() {
		return scanInProgress.get();
	}

	public void scanFinished() {
		scanInProgress.set(false);
	}

	public AtomicBoolean getScanInProgress() {
		return scanInProgress;
	}

	private void resetViews(IssuesTree issuesTree, LicensesTree licensesTree) {
		ComponentIssueDetails.getInstance().recreateComponentDetails();
		ComponentLicenseDetails.getInstance().recreateComponentDetails();
		issuesTree.reset();
		licensesTree.reset();
	}
}
