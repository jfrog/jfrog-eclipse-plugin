package org.jfrog.eclipse.utils;

import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.aether.graph.DependencyNode;

public class Utils {
	
	public static void GetProjects() {
		IWorkspace iworkspace = org.eclipse.core.resources.ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = iworkspace.getRoot();
		IProject[] projects = root.getProjects();
		IPath fullPath = root.getFullPath();
		System.out.println("Current working directory : " + fullPath);
		if (projects.length > 0) {
			for (IProject project : projects) {
				System.out.println(project.isOpen());
				try {
					IProjectDescription	description = project.getDescription();
					System.out.println(description);
					try {
						IProjectNature nature = project.getNature("org.eclipse.m2e.core.maven2Nature");
						if (nature != null) {
							System.out.println("Maven");
							// TODO maven logic
							DependencyNode dn = mavenDependencyTree(nature);
							List<org.eclipse.aether.graph.DependencyNode> listDepNod = dn.getChildren();
							System.out.println("Test:");
							printDependencies(listDepNod);
						}

					} catch (CoreException ce) {
						// Ignore
						System.out.println(ce);
					}
					try {
						IProjectNature nature = project.getNature("org.eclipse.buildship.core.gradleprojectnature");
						if (nature != null) {
							//TODO Gradle logic
							System.out.println("Gradle");
						}
					} catch (CoreException ce) {
						// Ignore
						System.out.println(ce);
					} 
				} catch (CoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	private static org.eclipse.aether.graph.DependencyNode mavenDependencyTree(IProjectNature nature) {
//		IProject project = nature.getProject();
		IMavenProjectFacade facade = null;  
	    try {
	      //  MavenProject mavenPrj = readMavenProject(null);
	    	facade = MavenPlugin.getMavenProjectRegistry().getProject(nature.getProject());
	    	MavenProject mavenProject = facade.getMavenProject(new NullProgressMonitor());
	    	org.eclipse.aether.graph.DependencyNode root = MavenPlugin.getMavenModelManager().readDependencyTree(facade, mavenProject, Artifact.SCOPE_TEST, new NullProgressMonitor());
			return root;
	    } catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    return null;
	}

	public static void printDependencies(List<org.eclipse.aether.graph.DependencyNode> listDepNod) {
		for (org.eclipse.aether.graph.DependencyNode depNode : listDepNod) {
			System.out.println("The dependency is:" + depNode.getArtifact().getGroupId() + ":" + depNode.getArtifact().getArtifactId() + ":" + depNode.getArtifact().getVersion());
			if (depNode.getChildren().size() > 0) {
				System.out.println("-------");
				printDependencies(depNode.getChildren());
			}
			System.out.println("-------");
		}
		
	}


//	private static MavenProject readMavenProject(IProgressMonitor monitor) throws CoreException {
//		if(monitor == null) {
//		      monitor = new NullProgressMonitor();
//		    }
//
//		    final IMaven maven = MavenPlugin.getMaven();
//
//		    IMavenExecutionContext context = maven.createExecutionContext();
//		    MavenExecutionRequest request = context.getExecutionRequest();
//		    request.setOffline(false);
//		    request.setUpdateSnapshots(false);
//		    request.setRecursive(false);
//
//		    MavenExecutionResult result = context.execute(new ICallable<MavenExecutionResult>() {
//		      public MavenExecutionResult call(IMavenExecutionContext context, IProgressMonitor monitor) throws CoreException {
//		        
//				return maven.readMavenProject(pomFile, context.newProjectBuildingRequest());
//		      }
//		    }, monitor);
//
//		    MavenProject project = result.getProject();
//		    if(project != null) {
//		      return project;
//		    }
//			return project;
//	}
}

