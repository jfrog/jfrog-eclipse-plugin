package com.jfrog.ide.eclipse.utils;

import java.util.Comparator;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.jfrog.build.extractor.scan.DependencyTree;
import org.jfrog.build.extractor.scan.GeneralInfo;


/**
 * Map of projects. Used in the UI to show the projects sorted by project name
 * and path.
 * 
 * @author yahavi
 */
public class ProjectsMap extends TreeMap<ProjectsMap.ProjectKey, DependencyTree> {
	private static final long serialVersionUID = 1L;

	/**
	 * Put a project in the map.
	 * 
	 * @param projectName      - The project name.
	 * @param dependencyTree - The dependencies tree.
	 * @return dependencyTree.
	 */
	public DependencyTree put(String projectName, DependencyTree dependencyTree) {
		return super.put(createKey(projectName, dependencyTree.getGeneralInfo()), dependencyTree);
	}
	
	public static ProjectKey createKey(String projectName, GeneralInfo generalInfo) {
		return new ProjectKey(projectName, generalInfo.getPath());
	}

	/**
	 * Comparable key. The keys are sorted by name and path.
	 */
	public static class ProjectKey implements Comparable<ProjectKey> {
		private String projectName;
		private String projectPath;

		public ProjectKey(String projectName, String projectPath) {
			this.projectName = projectName;
			this.projectPath = projectPath;
		}

		@Override
		public int compareTo(ProjectKey other) {
			return Comparator
					.comparing((ProjectsMap.ProjectKey key) -> StringUtils.compare(key.projectName, other.projectName))
					.thenComparing(key -> StringUtils.compare(key.projectPath, other.projectPath)).compare(this, other);
		}

		public String getProjectName() {
			return this.projectName;
		}

	}
}
