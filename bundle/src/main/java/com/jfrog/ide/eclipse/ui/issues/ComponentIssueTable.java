package com.jfrog.ide.eclipse.ui.issues;

import static com.jfrog.ide.eclipse.ui.UiUtils.createLabel;
import static com.jfrog.ide.eclipse.ui.UiUtils.setGridLayout;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.jfrog.build.extractor.scan.DependencyTree;
import org.jfrog.build.extractor.scan.Issue;
import org.jfrog.build.extractor.scan.Severity;

import com.jfrog.ide.common.filter.FilterManager;
import com.jfrog.ide.eclipse.ui.FilterManagerSingleton;
import com.jfrog.ide.eclipse.ui.IconManager;
import com.jfrog.ide.eclipse.ui.Panel;

import com.google.common.collect.Sets;

/**
 * @author yahavi
 */
public class ComponentIssueTable extends Panel {

	private TableColumnLayout tableLayout;
	private TableViewer tableViewer;
	private Panel tablePanel;

	public ComponentIssueTable(Composite parent) {
		super(parent);
		setGridLayout(this, 1, true);
		createLabel(this, "Component Issues Details");

		tablePanel = new Panel(this);
		setGridLayout(tablePanel, 1, true);
		tableLayout = new TableColumnLayout();
		tablePanel.setLayout(tableLayout);

		createComponentsIssuesTable(tablePanel);
	}

	private void createComponentsIssuesTable(Composite tableComposite) {
		tableViewer = new TableViewer(tableComposite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		Table table = tableViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		tableViewer.setContentProvider(ArrayContentProvider.getInstance());
		createColumns();
	}

	private void createColumns() {
		createTableViewerColumn("Severity", 0, new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				Issue issue = (Issue) element;
				return issue.getSeverity().getSeverityName();
			}

			@Override
			public Image getImage(Object element) {
				Severity severity = ((Issue) element).getSeverity();
				return IconManager.load(severity.name().toLowerCase());
			}
		});

		createTableViewerColumn("Summary", 6, new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				Issue issue = (Issue) element;
				return issue.getSummary();
			}
		});

		createTableViewerColumn("Issue Type", 0, new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				Issue issue = (Issue) element;
				return issue.getIssueType();
			}
		});

		createTableViewerColumn("Component", 4, new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				Issue issue = (Issue) element;
				return issue.getComponent();
			}
		});
		
		createTableViewerColumn("Fixed Versions", 0, new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				Issue issue = (Issue) element;
                List<String> fixedVersions = ListUtils.emptyIfNull(issue.getFixedVersions());
                return StringUtils.defaultIfEmpty(String.join(", ", fixedVersions), "[]");                
			}
		});
	}

	private TableViewerColumn createTableViewerColumn(String title, int weight, ColumnLabelProvider labelProvider) {
		TableViewerColumn viewerColumn = new TableViewerColumn(tableViewer, SWT.CENTER);
		TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.pack();
		tableLayout.setColumnData(column, new ColumnWeightData(weight, column.getWidth() + 15));
		viewerColumn.setLabelProvider(labelProvider);
		return viewerColumn;
	}

	public void updateIssuesTable(List<DependencyTree> selectedNodes) {
		Set<Issue> issuesSet = Sets.newHashSet();
		FilterManager filterManager = FilterManagerSingleton.getInstance();
		issuesSet.addAll(filterManager.getFilteredScanIssues(selectedNodes));
		
		tableViewer.setInput(
				issuesSet.stream().sorted(Comparator.comparing(issue -> ((Issue) issue).getSeverity()).reversed())
						.collect(Collectors.toList()));
	}
}
