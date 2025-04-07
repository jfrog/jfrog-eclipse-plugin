package com.jfrog.ide.eclipse.ui.issues;

import java.util.ArrayList;

import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.jfrog.ide.common.nodes.FileIssueNode;
import com.jfrog.ide.common.nodes.FileTreeNode;
import com.jfrog.ide.eclipse.ui.SearchableTree;

/**
 * @author yahavi
 */
public class IssuesTree extends SearchableTree {

	private static IssuesTree instance;
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
	protected void onClick(FileIssueNode selection) {
		componentDetails.createDetailsView(selection);
	}

	@Override
	public void applyFiltersForAllProjects() {
		treeViewer.setInput(scanResults);
	}

	@Override
	public void reset() {
		super.reset();
		issuesCountColumn.getColumn().setText("Issues");
		treeViewer.setInput(new ArrayList<FileTreeNode>());
	}

	public static void disposeTree() {
		if (instance != null) {
			instance.dispose();
		}
	}
}
