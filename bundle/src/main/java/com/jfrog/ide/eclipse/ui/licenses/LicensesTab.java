package com.jfrog.ide.eclipse.ui.licenses;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;

import com.jfrog.ide.eclipse.ui.ComponentDetails;

/**
 * @author yahavi
 */
public class LicensesTab {

	public LicensesTab(CTabFolder parent) {
		CTabItem tab = new CTabItem(parent, SWT.NONE);
		tab.setText("Licenses Info");
		SashForm horizontalDivision = new SashForm(parent, SWT.HORIZONTAL);

		// Left
		LicensesTree.createLicensesTree(horizontalDivision);

		// Right
		ComponentDetails componentDetails = ComponentLicenseDetails.create(horizontalDivision);

		LicensesTree licensesTree = LicensesTree.getLicensesTree();
		licensesTree.setComponentDetails(componentDetails);
		horizontalDivision.setWeights(new int[] { 1, 2 });
		tab.setControl(horizontalDivision);
	}
}
