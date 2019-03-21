package org.jfrog.eclipse.ui;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.jfrog.eclipse.configuration.XrayServerConfigImpl;
import org.jfrog.eclipse.scan.ScanManagersFactory;
import org.jfrog.eclipse.ui.actions.Filter.FilterType;
import org.jfrog.eclipse.ui.issues.IssuesTab;
import org.jfrog.eclipse.ui.licenses.LicensesTab;

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
		doQuickScan(parent);
	}

	private void createTabs(Composite parent, XrayScanToolbar xrayScanToolbar) {
		CTabFolder tabFolder = new CTabFolder(parent, SWT.CENTER);
		tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));

		new IssuesTab(tabFolder);
		LicensesTab licensesTab = new LicensesTab(tabFolder);
		tabFolder.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				xrayScanToolbar.setFilterType(
						tabFolder.getSelection().equals(licensesTab) ? FilterType.License : FilterType.Severity);
			}
		});
	}

	private void doQuickScan(Composite parent) {
		if (XrayServerConfigImpl.getInstance().areCredentialsSet()) {
			ScanManagersFactory.getInstance().startScan(parent, true);
		}
	}

	@PreDestroy
	public void dispose() {
		IconManager.dispose();
	}
}
