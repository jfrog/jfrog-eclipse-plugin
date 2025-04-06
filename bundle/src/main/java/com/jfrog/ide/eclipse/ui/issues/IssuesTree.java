package com.jfrog.ide.eclipse.ui.issues;

import java.util.ArrayList;

import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.jfrog.build.extractor.scan.DependencyTree;

import com.google.common.collect.Lists;
import com.jfrog.ide.common.nodes.FileTreeNode;
import com.jfrog.ide.eclipse.ui.SearchableTree;

/**
 * @author yahavi
 */
public class IssuesTree extends SearchableTree {

	private static IssuesTree instance;
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
		issuesCountColumn = createColumn("Issues", new IssueCountColumnLabelProvider(this), SWT.RIGHT, 0);
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
	public void applyFiltersForAllProjects() {
	}

	@Override
	public void reset() {
		super.reset();
		componentIssueTable.updateIssuesTable(Lists.newArrayList());
		issuesCountColumn.getColumn().setText("Issues");
		treeViewer.setInput(new ArrayList<FileTreeNode>());
	}

	public static void disposeTree() {
		if (instance != null) {
			instance.dispose();
		}
	}
}
