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
import org.jfrog.build.extractor.scan.DependencyTree;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jfrog.ide.common.configuration.JfrogCliDriver;
import com.jfrog.ide.common.filter.FilterManager;
import com.jfrog.ide.common.log.ProgressIndicator;
import com.jfrog.ide.common.scan.ComponentPrefix;
import com.jfrog.ide.eclipse.configuration.XrayServerConfigImpl;
import com.jfrog.ide.eclipse.log.Logger;
import com.jfrog.ide.eclipse.log.ProgressIndicatorImpl;
import com.jfrog.ide.eclipse.scheduling.ScanJob;
import com.jfrog.ide.eclipse.ui.FilterManagerSingleton;
import com.jfrog.ide.eclipse.ui.issues.IssuesTree;
import com.jfrog.ide.eclipse.utils.ProjectsMap;
import com.jfrog.xray.client.services.summary.Components;
import org.jfrog.build.api.util.Log;

/**
 * @author yahavi
 */
public abstract class ScanManager {

	static final Path HOME_PATH = Paths.get(System.getProperty("user.home"), ".jfrog-eclipse-plugin");
	private IProgressMonitor monitor;
	IProject project;
	Log log;
	JfrogCliDriver cliDriver;
	
	ScanManager(IProject project, ComponentPrefix prefix) throws IOException {
		this.project = project;
		Files.createDirectories(HOME_PATH);
		log = Logger.getInstance();
	}

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
	
	public IProject getIProject() {
		return project;
	}
	
	public void setMonitor(IProgressMonitor monitor) {
		this.monitor = monitor;
	}
	
	public IProgressMonitor getMonitor(){
		return monitor;
	}

	/**
	 * Schedule a dependency scan.
	 * 
	 * @param quickScan - True iff this is a quick scan.
	 * @param parent    - The parent UI composite. Cancel the scan if the parent is
	 *                  disposed.
	 */
	public void scanAndUpdateResults(boolean quickScan, IssuesTree issuesTree, Composite parent) {
		ScanJob.doSchedule(project.getName(), new ScanRunnable(parent, issuesTree, quickScan));
	}

	/**
	 * Start a dependency scan.
	 */
	private class ScanRunnable implements ICoreRunnable {
		private IssuesTree issuesTree;
		private boolean quickScan;
		private Composite parent;

		private ScanRunnable(Composite parent, IssuesTree issuesTree, boolean quickScan) {
			this.parent = parent;
			this.issuesTree = issuesTree;
			this.quickScan = quickScan;
		}

		@Override
		public void run(IProgressMonitor monitor) throws CoreException {
			// TODO: implement scan manager using JfrogCliDriver
		}

		private void setScanResults() {
			// TODO: re implement using SarifParser
		}
		
		private boolean isDisposed() {
			return parent == null || parent.isDisposed();
		}
	}

}
