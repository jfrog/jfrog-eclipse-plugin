package com.jfrog.ide.eclipse.scan;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Composite;
import org.jfrog.build.extractor.executor.CommandExecutor;
import org.jfrog.build.extractor.executor.CommandResults;
import org.jfrog.build.extractor.scan.DependencyTree;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jfrog.ide.common.configuration.JfrogCliDriver;
import com.jfrog.ide.common.filter.FilterManager;
import com.jfrog.ide.common.log.ProgressIndicator;
import com.jfrog.ide.common.nodes.FileTreeNode;
import com.jfrog.ide.common.parse.SarifParser;
import com.jfrog.ide.common.scan.ComponentPrefix;
import com.jfrog.ide.common.configuration.ServerConfig;
import com.jfrog.ide.eclipse.configuration.XrayServerConfigImpl;
import com.jfrog.ide.eclipse.log.Logger;
import com.jfrog.ide.eclipse.log.ProgressIndicatorImpl;
import com.jfrog.ide.eclipse.scheduling.ScanJob;
import com.jfrog.ide.eclipse.ui.FilterManagerSingleton;
import com.jfrog.ide.eclipse.ui.issues.IssuesTree;
import com.jfrog.ide.eclipse.utils.ProjectsMap;
import com.jfrog.ide.eclipse.utils.ProjectsMap.ProjectKey;
import com.jfrog.xray.client.services.summary.Components;
import org.jfrog.build.api.util.Log;

/**
 * @author yahavi
 */
public class ScanManager {
	static final Path HOME_PATH = Paths.get(System.getProperty("user.home"), ".jfrog-eclipse-plugin");
	private IProgressMonitor monitor;
	IWorkspace iworkspace;
	IProject[] projects;
	Log log;
	JfrogCliDriver cliDriver;
	SarifParser sarifParser;
	
	public ScanManager() throws IOException {
		this.iworkspace = ResourcesPlugin.getWorkspace();
		this.projects = iworkspace.getRoot().getProjects();
		Files.createDirectories(HOME_PATH);
		this.log = Logger.getInstance();
		this.cliDriver = new JfrogCliDriver(System.getenv(), HOME_PATH.toString(), log); // TODO: use the singleton implemented by 
		this.sarifParser = new SarifParser(log);
	}
	
	
	public void runScan(boolean isDebugLogs) {

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
	public void scanAndUpdateResults(boolean quickScan, IssuesTree issuesTree, Composite parent, boolean isDebugLogs) {
		ScanJob.doSchedule("ScanJob", new ScanRunnable(parent, issuesTree, quickScan, isDebugLogs)); //TODO: change name
	}

	/**
	 * Start an audit scan.
	 */
	private class ScanRunnable implements ICoreRunnable {
		private IssuesTree issuesTree;
		private boolean quickScan;
		private Composite parent;
		boolean isDebugLogs;

		private ScanRunnable(Composite parent, IssuesTree issuesTree, boolean quickScan, boolean isDebugLogs) {
			this.parent = parent;
			this.issuesTree = issuesTree;
			this.quickScan = quickScan;
			this.isDebugLogs = isDebugLogs;
		}

		@Override
		public void run(IProgressMonitor monitor) throws CoreException {
			Map<String, String> auditEnvVars = new HashMap<>();
			try {
				if (isDebugLogs) {
					auditEnvVars.put("JFROG_CLI_LOG_LEVEL", "DEBUG");
				}
				// TODO: get working dir = the root project, scannedDirectories = empty string for now, server-id (from singleton),  
//				String workingDir = iworkspace.getRoot().getLocation().toString(); // not working well
				ServerConfig server = XrayServerConfigImpl.getInstance();
				cliDriver.addCliServerConfig(server.getXrayUrl(), server.getArtifactoryUrl(), "eclipse-plugin", server.getUsername(), server.getPassword(), server.getAccessToken(), null, null);
				List<String> projectsListAsString = Arrays.stream(projects)
			             .map(IProject::getName)
			             .collect(Collectors.toList());
		        for (IProject project : projects) {
		            if (project.isOpen()) {
		                IPath projectPath = project.getLocation();
		                log.info(String.format("Performing scan on: %s", project.getName()));
		                
		    			CommandResults auditResults = cliDriver.runCliAudit(new File(projectPath.toString()), null, "eclipse-plugin", null, auditEnvVars);
		    			if (!auditResults.isOk()) {
		    				// log the issue to the problems tab
		    				log.error("Audit scan failed with an error: " + auditResults.getErr());
		    				return;
		    			}
		    			log.info("Finished audit scan successfully.\n" + auditResults.getRes());
		    			
		    			// update scan cache
		    			ScanCache.getInstance().updateScanResults(sarifParser.parse(auditResults.getRes()));
		    			
		    			// update issues tree
		    			issuesTree.setScanResults();
		    			
		    			parent.getDisplay().syncExec(new Runnable() {
		    				@Override
		    				public void run() {
		    					if (monitor.isCanceled()) {
		    						return;
		    					}
		    					issuesTree.applyFilters(new ProjectKey("KEY", "VALUE")); // TODO: change implementation
		    				}
		    			});
		            }
	            }

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	

		private void setScanResults() {
			// TODO: re implement using SarifParser
		}
		
		private boolean isDisposed() {
			return parent == null || parent.isDisposed();
		}
		
	}

}
