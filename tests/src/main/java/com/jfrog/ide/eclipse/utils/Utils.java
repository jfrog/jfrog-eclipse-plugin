package com.jfrog.ide.eclipse.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

import com.jfrog.ide.eclipse.scan.GradleScanManager;

/**
 * Utils class for testing
 * @author alexeiv
 *
 */
public class Utils {

	/**
	 * Create an eclipse project object and opens the project within the testing workspace.
	 * @param projectName - The project name
	 * @param projectType - The project type (Gradle, Maven or Npm)
	 * @return the IProject object.
	 * @throws IOException
	 * @throws CoreException
	 */
	public static IProject createProject(String projectName, String projectType) throws IOException, CoreException {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		File dir = new File(workspace.getRoot().getLocation().toFile(), projectName);
		if (dir.exists()) {
			dir.delete();
		}
		dir.mkdirs();
		File src = new File("resources/projects/" + projectType + "/" + projectName);
		FileUtils.copyDirectory(src, dir);
		final IProject project = workspace.getRoot().getProject(projectName);
		workspace.run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				if (!project.exists()) {
					IProjectDescription projectDescription = workspace.newProjectDescription(project.getName());
					projectDescription.setLocation(new Path(src.getAbsolutePath()));
					project.create(monitor);
					project.open(IResource.NONE, new NullProgressMonitor());
				} else {
					project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
				}
			}
		}, null);
		return project;
	}

	/**
	 * Returns the gradle init script file location
	 */
	public static String getGradleInitScriptLocation(GradleScanManager gradleScanManager) throws IOException {
		File currentDir = new File(System.getProperty("user.dir"));
		File parentDir = currentDir.getParentFile();
		File pathToGradleScriptFile = Paths.get(parentDir.getAbsolutePath(), "bundle", "src", "main", "resources", "gradle", GradleScanManager.GRADLE_INIT_SCRIPT).toFile();
		return gradleScanManager.createGradleFile(new FileInputStream(pathToGradleScriptFile));
	}

	/**
	 * Returns the expected result content from the test resources. 
	 * @param folderName - The folder name where the file located
	 * @param fileName - The file name that the content need to be returned.
	 * @return the content of the file.
	 * @throws IOException
	 */
	public static byte[] getResultContent(String folderName, String fileName) throws IOException {
		File expected = new File("resources/results/" + folderName + "/" + fileName);
		return Files.readAllBytes(expected.toPath());
	}
}
