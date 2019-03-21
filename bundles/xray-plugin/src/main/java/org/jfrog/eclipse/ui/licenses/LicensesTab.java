package org.jfrog.eclipse.ui.licenses;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.jfrog.eclipse.ui.ComponentDetails;

/**
 * @author yahavi
 */
public class LicensesTab extends CTabItem {

	public LicensesTab(CTabFolder parent) {
		super(parent, SWT.NONE);
		setText("Licenses Info");
		SashForm horizontalDivision = new SashForm(parent, SWT.HORIZONTAL);

		// Left
		LicensesTree.createLicensesTree(horizontalDivision);

		// Right
		ComponentDetails componentDetails = ComponentLicenseDetails.create(horizontalDivision);

		LicensesTree licensesTree = LicensesTree.getLicensesTree();
		licensesTree.setComponentDetails(componentDetails);
		horizontalDivision.setWeights(new int[] { 1, 2 });
		setControl(horizontalDivision);
	}
	
	@Override
	public void dispose() {
		ComponentLicenseDetails.disposeComponentDetails();
		LicensesTree.disposeTree();
		super.dispose();
	}
}
