package org.jfrog.eclipse.ui.issues;

import java.util.Map.Entry;

import org.eclipse.swt.widgets.Composite;
import org.jfrog.build.extractor.scan.DependenciesTree;
import org.jfrog.eclipse.ui.SearchableTree;
import org.jfrog.filter.FilterManager;

/**
 * @author yahavi
 */
public class IssuesTree extends SearchableTree {

	private static IssuesTree instance;
	private ComponentIssueTable componentIssueTable;
	private static DependenciesTree root;

	public static void createIssuesTree(Composite parent) {
		instance = new IssuesTree(parent);
	}

	public static IssuesTree getIssuesTree() {
		return instance;
	}

	private IssuesTree(Composite parent) {
		super(parent, new IssuesTreeColumnLabelProvider());
		if (root == null) {
			root = new DependenciesTree();
		}
		applyFiltersForAllProjects();
	}

	@Override
	protected void onClick(DependenciesTree selection) {
		componentDetails.createDetailsView(selection);
		componentIssueTable.updateIssuesTable(getSelectedNodes());
	}

	public void setComponentIssueTable(ComponentIssueTable componentIssueTable) {
		this.componentIssueTable = componentIssueTable;
	}

	@Override
	public void applyFilters(String projectName) {
		DependenciesTree project = projects.get(projectName);
		if (project != null) {
			DependenciesTree filteredRoot = (DependenciesTree) project.clone();
			FilterManager filterManager = FilterManager.getInstance();
			filterManager.applyFilters(project, filteredRoot, new DependenciesTree("All components"));
			filteredRoot.setIssues(filteredRoot.processTreeIssues());
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
		treeViewer.setInput(root);
	}

	public static void disposeTree() {
		if (instance != null) {
			instance.dispose();
		}
	}
}
