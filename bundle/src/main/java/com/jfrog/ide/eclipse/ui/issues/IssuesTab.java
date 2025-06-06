package com.jfrog.ide.eclipse.ui.issues;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;

import com.jfrog.ide.eclipse.ui.ComponentDetails;

/**
 * @author yahavi
 */
public class IssuesTab {

	public IssuesTab(CTabFolder parent) {
		CTabItem tab = new CTabItem(parent, SWT.NONE);
		tab.setText("Issues");

		SashForm horizontalDivision = new SashForm(parent, SWT.HORIZONTAL);

		// Left
		IssuesTree.createIssuesTree(horizontalDivision);

		// Right
		SashForm verticalDivision = new SashForm(horizontalDivision, SWT.VERTICAL);
		ComponentDetails componentDetails = ComponentIssueDetails.createComponentIssueDetails(verticalDivision);

		registerTreeListeners(componentDetails);
		horizontalDivision.setWeights(new int[] { 1, 4 });
		parent.setSelection(tab);
		tab.setControl(horizontalDivision);
	}

	private void registerTreeListeners(ComponentDetails componentDetails) {
		IssuesTree issuesTree = IssuesTree.getInstance();
		issuesTree.setComponentDetails(componentDetails);
	}
}
