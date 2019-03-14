package org.jfrog.eclipse.ui.issues;

import static org.jfrog.eclipse.ui.UiUtils.createText;
import static org.jfrog.eclipse.ui.UiUtils.setGridLayout;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
import org.jfrog.build.extractor.scan.DependenciesTree;
import org.jfrog.build.extractor.scan.Issue;
import org.jfrog.build.extractor.scan.Severity;
import org.jfrog.eclipse.scan.ScanManagersFactory;
import org.jfrog.eclipse.ui.IconManager;
import org.jfrog.eclipse.ui.Panel;

import com.google.common.collect.Sets;

/**
 * @author yahavi
 */
public class ComponentIssueTable extends Panel {

	private TableColumnLayout tableLayout;
	private TableViewer tableViewer;

	public ComponentIssueTable(Composite parent) {
		super(parent);
		setGridLayout(this, 1, true);
		createText(this, "Component Issues Details");

		Panel tablePanel = new Panel(this);
		setGridLayout(tablePanel, 1, true);
		tableLayout = new TableColumnLayout();
		tablePanel.setLayout(tableLayout);

		createComponentsIssuesTable(tablePanel);
	}

	private void createComponentsIssuesTable(Composite tableComposite) {
		tableViewer = new TableViewer(tableComposite,
				SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
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

	public void updateIssuesTable(List<DependenciesTree> selectedNodes) {
		Set<Issue> issuesSet = Sets.newHashSet();
		ScanManagersFactory.getInstance().getScanManagers().forEach(scanManager -> {
			issuesSet.addAll(scanManager.getFilteredScanIssues(selectedNodes));
		});

		tableViewer.setInput(issuesSet.stream().sorted(Comparator.reverseOrder()).collect(Collectors.toList()));
	}
}
