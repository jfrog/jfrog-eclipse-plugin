package com.jfrog.ide.eclipse.scan;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Composite;
import org.jfrog.build.extractor.executor.CommandResults;

import com.jfrog.ide.common.configuration.JfrogCliDriver;
import com.jfrog.ide.common.parse.SarifParser;
import com.jfrog.ide.eclipse.configuration.CliDriverWrapper;
import com.jfrog.ide.eclipse.log.Logger;
import com.jfrog.ide.eclipse.scheduling.CliJob;
import com.jfrog.ide.eclipse.ui.issues.ComponentIssueDetails;
import com.jfrog.ide.eclipse.ui.issues.IssuesTree;
import org.jfrog.build.api.util.Log;

/**
 * @author yahavi
 */
public class ScanManager {
	static final Path HOME_PATH = Paths.get(System.getProperty("user.home"), ".jfrog-eclipse-plugin");
	private static ScanManager instance;
	private IProgressMonitor monitor;
	private IWorkspace iworkspace;
	private IProject[] projects;
	private Log log;
	private JfrogCliDriver cliDriver;
	private SarifParser sarifParser;
	private AtomicBoolean scanInProgress = new AtomicBoolean(false);
	
	private ScanManager() {
		this.iworkspace = ResourcesPlugin.getWorkspace();
		this.projects = iworkspace.getRoot().getProjects();
		this.log = Logger.getInstance();
		this.cliDriver = CliDriverWrapper.getInstance().getCliDriver();  
		this.sarifParser = new SarifParser(log);
	}
	
	public static synchronized ScanManager getInstance(){
        if (instance == null) {
            instance = new ScanManager();
        }
        return instance;
	}
	
	public void startScan(Composite parent, boolean isDebugLogs) {
		Map<String, String> auditEnvVars = new HashMap<>();
		
		// If scan is in progress - do not perform another scan
		if (isScanInProgress()) {
			log.info("Previous scan still running...");
			return;
		}
		
		scanInProgress.compareAndSet(false, true);
		resetIssuesView(IssuesTree.getInstance());
		ScanCache.getInstance().resetCache();
		
		// refresh projects list
		projects = iworkspace.getRoot().getProjects();
		if (projects.length == 0) {
			log.info("No projects to scan.");
		}

		if (isDebugLogs) {
			auditEnvVars.put("JFROG_CLI_LOG_LEVEL", "DEBUG");
			auditEnvVars.put("CI", "true");
		}

        for (IProject project : projects) {
        	scanAndUpdateResults(IssuesTree.getInstance(), parent, isDebugLogs, project, auditEnvVars);
        }
	}
	
	public void checkCanceled() {
		if (monitor != null && monitor.isCanceled()) {
			throw new CancellationException("Xray scan was canceled");
		}
	}

	public void setMonitor(IProgressMonitor monitor) {
		this.monitor = monitor;
	}
	
	public IProgressMonitor getMonitor(){
		return monitor;
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
	
	private void resetIssuesView(IssuesTree issuesTree) {
		ComponentIssueDetails.getInstance().recreateComponentDetails();
		issuesTree.reset();
	}

	/**
	 * Schedule an audit scan.
	 * 
	 * @param issuesTree - The issues tree object to present the issues found by the scan.
	 * @param parent    - The parent UI composite. Cancel the scan if the parent is
	 *                  disposed.
	 * @param isDebugLogs - If set to True, generate debug logs from the audit command.
	 * @param project - The scanned project object.
	 * @param envVars = The environment variables for running the audit command.                 
	 */
	public void scanAndUpdateResults(IssuesTree issuesTree, Composite parent, boolean isDebugLogs, IProject project, Map<String, String> envVars) {
		CliJob.doSchedule(String.format("Performing Scan: %s", project.getName()), new ScanRunnable(parent, issuesTree, isDebugLogs, project, envVars)); 
	}

	/**
	 * Start an audit scan.
	 */
	private class ScanRunnable implements ICoreRunnable {
		private IssuesTree issuesTree;
		private Composite parent;
		private IProject project;
		private Map<String, String> envVars;
		private boolean isDebugLogs;
		

		private ScanRunnable(Composite parent, IssuesTree issuesTree, boolean isDebugLogs, IProject project, Map<String, String> envVars) {
			this.parent = parent;
			this.issuesTree = issuesTree;
			this.project = project;
			this.envVars = envVars;
			this.isDebugLogs = isDebugLogs;
		}

		@Override
		public void run(IProgressMonitor monitor) throws CoreException {
			ScanManager.this.monitor = monitor;
			if (isDisposed()) {
				return;
			}
			
			log.info(String.format("Performing scan on: %s", project.getName()));
			
			try {
		            if (project.isOpen()) {
		                IPath projectPath = project.getLocation();
		    			CommandResults auditResults = cliDriver.runCliAudit(new File(projectPath.toString()), null, CliDriverWrapper.CLIENT_ID_SERVER, null, envVars);
		    			if (!auditResults.isOk()) {
		    				// log the issue to the problems tab
		    				log.error("Audit scan failed with an error: " + auditResults.getErr());
		    				return;
		    			}
		    			
		    			checkCanceled();
		    			
		    			log.info("Finished audit scan successfully.\n" + auditResults.getRes());
		    			if (isDebugLogs) {
		    				log.debug(auditResults.getErr());
		    			}
		    			
		    			// update scan cache
		    			log.info("Updating scan cache.");
		    			ScanCache.getInstance().updateScanResults(sarifParser.parse(auditResults.getRes()));
		    			
		    			// TODO: update issues tree
		            }
			} catch (CancellationException ce) {
				log.info(ce.getMessage());
			} catch (Exception e) {
				CliDriverWrapper.getInstance().showCliError("An error occurred while performing audit scan", e);
			} finally {
				scanFinished();
			}
		}
		
		private boolean isDisposed() {
			return parent == null || parent.isDisposed();
		}
		
	}
}