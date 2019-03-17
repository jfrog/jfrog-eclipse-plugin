package org.jfrog.eclipse.ui.issues;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.jfrog.eclipse.ui.ComponentDetails;

/**
 * @author yahavi
 */
public class IssuesTab extends CTabItem {

	public IssuesTab(CTabFolder parent) {
		super(parent, SWT.NONE);
		setText("Issues");

		SashForm horizontalDivision = new SashForm(parent, SWT.HORIZONTAL);

		// Left
		IssuesTree.createIssuesTree(horizontalDivision);

		// Right
		SashForm verticalDivision = new SashForm(horizontalDivision, SWT.VERTICAL);
		ComponentDetails componentDetails = ComponentIssueDetails.create(verticalDivision);
		ComponentIssueTable componentIssueTable = new ComponentIssueTable(verticalDivision);

		registerTreeListeners(componentDetails, componentIssueTable);
		horizontalDivision.setWeights(new int[] { 1, 2 });
		parent.setSelection(this);
		setControl(horizontalDivision);
	}
	
	private void registerTreeListeners(ComponentDetails componentDetails, ComponentIssueTable componentIssueTable) {
		IssuesTree issuesTree = IssuesTree.getIssuesTree();
		issuesTree.setComponentDetails(componentDetails);
		issuesTree.setComponentIssueTable(componentIssueTable);
		issuesTree.registerListeners();
	}
	
	@Override
	public void dispose() {
		ComponentIssueDetails.disposeComponentDetails();
		IssuesTree.disposeTree();
		super.dispose();
	}
}
