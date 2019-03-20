package org.jfrog.eclipse.test.utils;

import java.io.File;
import java.io.IOException;

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

public class Utils {
	
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

}
