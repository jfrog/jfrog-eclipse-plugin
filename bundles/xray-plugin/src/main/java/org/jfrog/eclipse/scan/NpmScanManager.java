package org.jfrog.eclipse.scan;

import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.jfrog.build.extractor.npm.NpmDriver;
import org.jfrog.build.extractor.npm.extractor.NpmDependencyTree;
import org.jfrog.build.extractor.scan.DependenciesTree;
import org.jfrog.build.extractor.scan.GeneralInfo;
import org.jfrog.scan.ComponentPrefix;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

/**
 * @author yahavi
 */
public class NpmScanManager extends ScanManager {

	private static NpmDriver npmDriver = new NpmDriver("", null);
	private static ObjectMapper objectMapper = new ObjectMapper();

	NpmScanManager(IProject project) throws IOException {
		super(project, ComponentPrefix.NPM);
		getLog().info("Found npm project: " + getProjectName());
	}

	@Override
	void refreshDependencies() throws IOException {
	}

	@Override
	void buildTree() throws CoreException, JsonProcessingException, IOException {
		JsonNode jsonRoot = npmDriver.list(project.getFullPath().toFile(), Lists.newArrayList());
		DependenciesTree rootNode = (DependenciesTree) NpmDependencyTree.createDependenciesTree(null, jsonRoot);
		JsonNode packageJson = objectMapper.readTree(project.getFile("package.json").getFullPath().toFile());
		String name = packageJson.get("name").asText();
		String version = packageJson.get("version").asText();
		rootNode.setUserObject(name);
		GeneralInfo generalInfo = new GeneralInfo().artifactId(name + ":" + version).version(version).pkgType("Npm")
				.path(project.getFullPath().toString());
		rootNode.setGeneralInfo(generalInfo);
		setScanResults(rootNode);
	}
}
