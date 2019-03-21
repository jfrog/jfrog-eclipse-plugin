package org.jfrog.eclipse.scan;

import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.jfrog.npm.NpmTreeBuilder;
import org.jfrog.scan.ComponentPrefix;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * @author yahavi
 */
public class NpmScanManager extends ScanManager {

	private NpmTreeBuilder npmTreeBuilder;
	
	NpmScanManager(IProject project) throws IOException {
		super(project, ComponentPrefix.NPM);
		getLog().info("Found npm project: " + getProjectName());
		npmTreeBuilder = new NpmTreeBuilder(project.getFullPath().toFile().toPath());
	}

	@Override
	void refreshDependencies(IProgressMonitor monitor) throws IOException {
	}

	@Override
	void buildTree() throws CoreException, JsonProcessingException, IOException {
		setScanResults(npmTreeBuilder.buildTree(getLog()));
	}
}
