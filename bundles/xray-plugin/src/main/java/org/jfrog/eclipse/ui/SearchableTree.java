package org.jfrog.eclipse.ui;

import java.util.Arrays;
import java.util.HashMap;
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
import org.jfrog.build.extractor.scan.DependenciesTree;

import com.google.common.collect.Lists;

/**
 * @author yahavi
 */
public abstract class SearchableTree extends FilteredTree {

	protected HashMap<String, DependenciesTree> projects = new HashMap<String, DependenciesTree>();
	protected ComponentDetails componentDetails;
	private TreeColumnLayout treeLayout = new TreeColumnLayout();

	public SearchableTree(Composite parent, ColumnLabelProvider labelProvider) {
		super(parent, true);
		init(SWT.BORDER, createFilter());
		setQuickSelectionMode(true);
		treeViewer.setContentProvider(new ScanTreeContentProvider());
		treeViewer.getTree().setHeaderVisible(true);
		createColumn(labelProvider);
	}

	@Override
	protected Control createTreeControl(Composite parent, int style) {
		Control treeControl = super.createTreeControl(parent, style);
		treeControl.setLayoutData(null);
		treeControl.getParent().setLayout(treeLayout);
		return treeControl;
	}

	public void registerListeners() {
		treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				if (event.getSelection().isEmpty()) {
					return;
				}
				DependenciesTree selection = (DependenciesTree) treeViewer.getStructuredSelection().getFirstElement();
				onClick(selection);
			}
		});
	}

	public void setComponentDetails(ComponentDetails componentDetails) {
		this.componentDetails = componentDetails;
	}

	protected abstract void onClick(DependenciesTree selection);

	private static PatternFilter createFilter() {
		PatternFilter patternFilter = new PatternFilter();
		patternFilter.setIncludeLeadingWildcard(true);
		return patternFilter;
	}

	private void createColumn(ColumnLabelProvider labelProvider) {
		TreeViewerColumn viewerColumn = new TreeViewerColumn(treeViewer, SWT.NONE);
		viewerColumn.getColumn().setText("Components Tree");
		viewerColumn.setLabelProvider(labelProvider);
		treeLayout.setColumnData(viewerColumn.getColumn(),
				new ColumnWeightData(1, viewerColumn.getColumn().getWidth()));
	}

	public List<DependenciesTree> getSelectedNodes() {
		List<DependenciesTree> selectedNodes = Lists.newArrayList();
		TreePath[] selectionPaths = treeViewer.getStructuredSelection().getPaths();
		if (selectionPaths != null) {
			selectedNodes = Arrays.stream(selectionPaths)
					.map(selectedPath -> (DependenciesTree) selectedPath.getLastSegment()).collect(Collectors.toList());
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
		projects.clear();
	}

	public void addScanResults(DependenciesTree scanTree, String projectName) {
		projects.put(projectName, scanTree);
	}

	public abstract void applyFilters(String projectName);

	public abstract void applyFiltersForAllProjects();
}
