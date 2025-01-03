package com.jfrog.ide.eclipse.ui.issues;

import java.util.Map.Entry;

import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.jfrog.build.extractor.scan.DependencyTree;

import com.google.common.collect.Lists;
import com.jfrog.ide.common.filter.FilterManager;
import com.jfrog.ide.eclipse.ui.FilterManagerSingleton;
import com.jfrog.ide.eclipse.ui.SearchableTree;
import com.jfrog.ide.eclipse.utils.ProjectsMap.ProjectKey;

/**
 * @author yahavi
 */
public class IssuesTree extends SearchableTree {

	private static IssuesTree instance;

	private DependencyTree root = new DependencyTree();
	private ComponentIssueTable componentIssueTable;
	private TreeViewerColumn issuesCountColumn;

	public static void createIssuesTree(Composite parent) {
		instance = new IssuesTree(parent);
	}

	public static IssuesTree getInstance() {
		return instance;
	}

	private IssuesTree(Composite parent) {
		super(parent, new IssuesTreeColumnLabelProvider());
		issuesCountColumn = createColumn("Issues (0)", new IssueCountColumnLabelProvider(this), SWT.RIGHT, 0);
		applyFiltersForAllProjects();
	}

	@Override
	protected void onClick(DependencyTree selection) {
		componentDetails.createDetailsView(selection);
		componentIssueTable.updateIssuesTable(getSelectedNodes());
	}

	public void setComponentIssueTable(ComponentIssueTable componentIssueTable) {
		this.componentIssueTable = componentIssueTable;
	}

	@Override
	public void applyFilters(ProjectKey projectKey) {
		DependencyTree project = projects.get(projectKey);
		if (project != null) {
			FilterManager filterManager = FilterManagerSingleton.getInstance();
			DependencyTree filteredRoot = filterManager.applyFilters(project);
			filteredRoot.setIssues(filteredRoot.processTreeIssues());
			root.add(filteredRoot);
			if (root.getChildCount() == 1) {
				// If there is only one project - Show only its dependencies in the tree viewer.
				treeViewer.setInput(filteredRoot);
			} else {
				treeViewer.setInput(root);
			}
			long totalIssues = root.getChildren().stream().mapToInt(DependencyTree::getIssueCount).sum();
			issuesCountColumn.getColumn().setText("Issues (" + totalIssues + ")");
		}
	}

	@Override
	public void applyFiltersForAllProjects() {
		root = new DependencyTree();
		for (Entry<ProjectKey, DependencyTree> entry : projects.entrySet()) {
			applyFilters(entry.getKey());
		}
	}

	@Override
	public void reset() {
		super.reset();
		componentIssueTable.updateIssuesTable(Lists.newArrayList());
		issuesCountColumn.getColumn().setText("Issues (0)");
		root = new DependencyTree();
		treeViewer.setInput(root);
	}

	public static void disposeTree() {
		if (instance != null) {
			instance.dispose();
		}
	}
}
