package com.jfrog.ide.eclipse.ui;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import com.jfrog.ide.eclipse.ui.actions.Filter.FilterType;
import com.jfrog.ide.eclipse.ui.issues.ComponentIssueDetails;
import com.jfrog.ide.eclipse.ui.issues.IssuesTab;
import com.jfrog.ide.eclipse.ui.issues.IssuesTree;

/**
 * The entry point of the plug-in. Creates the UI and perform a quick scan if
 * credentials set.
 * 
 * @author yahavi
 */
public class PartControl {

	@PostConstruct
	public void createPartControl(Composite parent) {
		UiUtils.setGridLayout(parent, 1, false);
		XrayScanToolbar xrayScanToolbar = new XrayScanToolbar(parent);

		createTabs(parent, xrayScanToolbar);
	}

	private void createTabs(Composite parent, XrayScanToolbar xrayScanToolbar) {
		CTabFolder tabFolder = new CTabFolder(parent, SWT.CENTER);
		tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));

		new IssuesTab(tabFolder);
		tabFolder.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				xrayScanToolbar.setFilterType(FilterType.values()[tabFolder.getSelectionIndex()]);
			}
		});
	}

	@PreDestroy
	public void dispose() {
		ComponentIssueDetails.disposeComponentDetails();
		IssuesTree.disposeTree();
		IconManager.dispose();
	}
}
