package com.jfrog.ide.eclipse.ui.licenses;

import java.util.Map.Entry;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.widgets.Composite;
import org.jfrog.build.extractor.scan.DependencyTree;

import com.jfrog.ide.common.filter.FilterManager;
import com.jfrog.ide.eclipse.ui.FilterManagerSingleton;
import com.jfrog.ide.eclipse.ui.SearchableTree;
import com.jfrog.ide.eclipse.utils.ProjectsMap;

/**
 * @author yahavi
 */
public class LicensesTree extends SearchableTree {

	private static LicensesTree instance;
	private DependencyTree root = new DependencyTree();;

	public static void createLicensesTree(Composite parent) {
		instance = new LicensesTree(parent);
	}

	public static LicensesTree getInstance() {
		return instance;
	}

	private LicensesTree(Composite parent) {
		super(parent, new ColumnLabelProvider());
		applyFiltersForAllProjects();
	}

	@Override
	protected void onClick(DependencyTree selection) {
		componentDetails.createDetailsView(selection);
	}

	@Override
	public void applyFilters(ProjectsMap.ProjectKey projectName) {
		DependencyTree project = projects.get(projectName);
		if (project != null) {
//			DependencyTree filteredRoot = (DependencyTree) project.clone(); TODO: delete if code works
			FilterManager filterManager = FilterManagerSingleton.getInstance();
			DependencyTree filteredRoot = filterManager.applyFilters(project);
			root.add(filteredRoot);
			if (root.getChildCount() == 1) {
				// If there is only one project - Show only its dependencies in the tree viewer.
				treeViewer.setInput(filteredRoot);
			} else {
				treeViewer.setInput(root);
			}
		}
	}

	@Override
	public void applyFiltersForAllProjects() {
		root.removeAllChildren();
		for (Entry<ProjectsMap.ProjectKey, DependencyTree> entry : projects.entrySet()) {
			applyFilters(entry.getKey());
		}
	}

	@Override
	public void reset() {
		super.reset();
		root.removeAllChildren();
		treeViewer.setInput(root);
	}

	public static void disposeTree() {
		if (instance != null) {
			instance.dispose();
		}
	}
}
