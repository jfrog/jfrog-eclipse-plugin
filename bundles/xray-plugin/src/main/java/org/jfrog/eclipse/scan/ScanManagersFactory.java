package org.jfrog.eclipse.scan;

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
import org.jfrog.eclipse.log.Logger;
import org.jfrog.eclipse.npm.NpmProject;
import org.jfrog.eclipse.scheduling.ScanJob;
import org.jfrog.eclipse.ui.issues.IssuesTree;
import org.jfrog.eclipse.ui.licenses.LicensesTree;
import org.jfrog.utils.Utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

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
			Logger.getLogger().info("Previous scan still running...");
			return;
		}
		refreshScanManagers(parent);

		// Cancel other jobs
		Job[] jobs = Job.getJobManager().find(ScanJob.FAMILY);
		if (ArrayUtils.isNotEmpty(jobs)) {
			for (Job job : jobs) {
				Logger.getLogger().info("Cancling previous running scan: " + job.getName());
				job.cancel();
			}
		}

		if (scanManagers.size() > 0) {
			setScanInProgress(true);
		}
		IssuesTree issuesTree = IssuesTree.getIssuesTree();
		LicensesTree licensesTree = LicensesTree.getLicensesTree();
		if (issuesTree == null || licensesTree == null) {
			return;
		}
		issuesTree.reset();
		licensesTree.reset();
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
				for (IProject project : projects) {
					if (!project.isOpen()) {
						Logger.getLogger().info("Project is closed: " + project.getName());
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
				Set<String> packageJsonDirs = Utils.findPackageJsonDirs(paths);
				for (String dir : packageJsonDirs) {
					IProject npmProject = new NpmProject(dir, iworkspace);
					scanManagers.add(new NpmScanManager(npmProject));
				}
			} catch (IOException e) {
				Logger.getLogger().error(e.getMessage(), e);
			}
		}
	}

	public boolean isScanInProgress() {
		return scanInProgress.get();
	}

	public void setScanInProgress(boolean isScanInProgress) {
		scanInProgress.set(isScanInProgress);
	}
}
