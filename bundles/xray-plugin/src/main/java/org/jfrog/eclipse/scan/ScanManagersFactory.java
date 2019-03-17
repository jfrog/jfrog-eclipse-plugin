package org.jfrog.eclipse.scan;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.swt.widgets.Composite;
import org.jfrog.eclipse.log.Logger;
import org.jfrog.eclipse.npm.NpmProject;
import org.jfrog.eclipse.ui.issues.IssuesTree;
import org.jfrog.eclipse.ui.licenses.LicensesTree;
import org.jfrog.utils.Utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * @author yahavi
 */
public class ScanManagersFactory {
	private static ScanManagersFactory instance = new ScanManagersFactory();
	private List<ScanManager> scanManagers = Lists.newArrayList();

	// Lock to prevent multiple simultaneous scans
	private AtomicBoolean scanInProgress = new AtomicBoolean(false);

	private ScanManagersFactory() {
	}

	public static ScanManagersFactory getInstance() {
		return instance;
	}

	public Collection<ScanManager> getScanManagers() {
		return scanManagers;
	}

	public void refreshScanManagers(Composite parent) {
		scanManagers = Lists.newArrayList();
		IWorkspace iworkspace = org.eclipse.core.resources.ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = iworkspace.getRoot();
		IProject[] projects = root.getProjects();
		Set<Path> paths = Sets.newHashSet();
		if (projects.length > 0) {
			try {
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
	
	public void startScan(Composite parent, boolean quickScan) {
		if (isScanInProgress()) {
			Logger.getLogger().info("Previous scan still running...");
			return;
		}
		refreshScanManagers(parent);
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

	public boolean isScanInProgress() {
		return scanInProgress.get();
	}

	public void setScanInProgress(boolean isScanInProgress) {
		scanInProgress.set(isScanInProgress);
	}
	
	public ScanManager getProjectScanManager(IProject project) {
		for (ScanManager scanManager : scanManagers) {
			if (scanManager.getIProject().getName().equals(project.getName())) {
				return scanManager;
			}
		}
		return null;
	}
}
