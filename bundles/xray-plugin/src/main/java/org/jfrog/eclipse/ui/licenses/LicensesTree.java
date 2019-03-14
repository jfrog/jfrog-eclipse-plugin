package org.jfrog.eclipse.ui.licenses;

import java.util.Map.Entry;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.widgets.Composite;
import org.jfrog.build.extractor.scan.DependenciesTree;
import org.jfrog.eclipse.ui.SearchableTree;
import org.jfrog.filter.FilterManager;

/**
 * @author yahavi
 */
public class LicensesTree extends SearchableTree {

	private static LicensesTree instance;
	private static DependenciesTree root;

	public static void createLicensesTree(Composite parent) {
		instance = new LicensesTree(parent);
	}

	public static LicensesTree getLicensesTree() {
		return instance;
	}

	private LicensesTree(Composite parent) {
		super(parent, new ColumnLabelProvider());
		if (root == null) {
			root = new DependenciesTree();
		}
		applyFiltersForAllProjects();
	}

	@Override
	protected void onClick(DependenciesTree selection) {
		componentDetails.createDetailsView(selection);
	}

	@Override
	public void applyFilters(String projectName) {
		DependenciesTree project = projects.get(projectName);
		if (project != null) {
			DependenciesTree filteredRoot = (DependenciesTree) project.clone();
			FilterManager filterManager = FilterManager.getInstance();
			filterManager.applyFilters(project, new DependenciesTree(), filteredRoot);
			root.add(filteredRoot);
			treeViewer.setInput(root);
		}
	}

	@Override
	public void applyFiltersForAllProjects() {
		root.removeAllChildren();
		for (Entry<String, DependenciesTree> entry : projects.entrySet()) {
			applyFilters(entry.getKey());
		}
	}

	@Override
	public void reset() {
		super.reset();
		root.removeAllChildren();
	}
	
	public static void disposeTree() {
		if (instance != null) {
			instance.dispose();
		}
	}
}
