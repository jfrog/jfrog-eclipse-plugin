package com.jfrog.ide.eclipse.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jface.layout.TreeColumnLayout;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.jfrog.build.extractor.scan.DependencyTree;

import com.google.common.collect.Lists;
import com.jfrog.ide.common.nodes.FileIssueNode;
import com.jfrog.ide.common.nodes.FileTreeNode;

/**
 * Base class for the issues tree.
 * 
 * @author yahavi
 */
public abstract class SearchableTree extends FilteredTree {

	private TreeColumnLayout treeLayout = new TreeColumnLayout();
	protected ComponentDetails componentDetails;
	protected List<FileTreeNode> scanResults = new ArrayList<FileTreeNode>();

	public SearchableTree(Composite parent, ColumnLabelProvider labelProvider) {
		super(parent, true);
		init(SWT.BORDER | SWT.MULTI, createFilter());
		setQuickSelectionMode(true);
		treeViewer.setContentProvider(new ScanTreeContentProvider());
		treeViewer.getTree().setHeaderVisible(true);
		createColumn("Components Tree", labelProvider, SWT.NONE, 1);
		registerListeners();
	}

	@Override
	protected Control createTreeControl(Composite parent, int style) {
		Control treeControl = super.createTreeControl(parent, style);
		treeControl.setLayoutData(null);
		treeControl.getParent().setLayout(treeLayout);
		return treeControl;
	}
	
	public TreeViewerColumn createColumn(String title, ColumnLabelProvider labelProvider, int style, int weight) {
		TreeViewerColumn viewerColumn = new TreeViewerColumn(treeViewer, style);
		viewerColumn.getColumn().setMoveable(false);
		viewerColumn.getColumn().setText(title);
		viewerColumn.setLabelProvider(labelProvider);
		viewerColumn.getColumn().pack();
		treeLayout.setColumnData(viewerColumn.getColumn(),
				new ColumnWeightData(weight, viewerColumn.getColumn().getWidth() + 20));
		return viewerColumn;
	}

	private void registerListeners() {
		treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				if (event.getSelection().isEmpty()) {
					return;
				}
				Object selectedElement = treeViewer.getStructuredSelection().getFirstElement();
				if (selectedElement instanceof FileIssueNode) {
					FileIssueNode issueNode = (FileIssueNode) selectedElement;
					onClick(issueNode);
				}
			}
		});
	}

	public void setComponentDetails(ComponentDetails componentDetails) {
		this.componentDetails = componentDetails;
	}

	protected abstract void onClick(FileIssueNode selection);

	private static PatternFilter createFilter() {
		PatternFilter patternFilter = new PatternFilter();
		patternFilter.setIncludeLeadingWildcard(true);
		return patternFilter;
	}

	public List<DependencyTree> getSelectedNodes() {
		List<DependencyTree> selectedNodes = Lists.newArrayList();
		TreePath[] selectionPaths = treeViewer.getStructuredSelection().getPaths();
		if (selectionPaths != null) {
			selectedNodes = Arrays.stream(selectionPaths)
					.map(selectedPath -> (DependencyTree) selectedPath.getLastSegment()).collect(Collectors.toList());
		}

		return selectedNodes;
	}

	public void collapseAll() {
		getViewer().collapseAll();
	}

	public void expandAll() {
		getViewer().expandAll();
	}

	public void reset() {
		scanResults.clear();
	}

	public void addScanResults(List<FileTreeNode> results) {
		scanResults.addAll(results);
	}

	public void showResultsOnTree() {
		treeViewer.setInput(scanResults);
	}

	public abstract void applyFiltersForAllProjects();
}
