package org.jfrog.eclipse.ui.licenses;

import org.eclipse.swt.widgets.Composite;
import org.jfrog.build.extractor.scan.DependenciesTree;
import org.jfrog.eclipse.ui.ComponentDetails;

/**
 * @author yahavi
 */
public class ComponentLicenseDetails extends ComponentDetails {
	public ComponentLicenseDetails(Composite parent) {
		super(parent, "Details");
	}

	@Override
	public void createDetailsView(DependenciesTree node) {
		createCommonPanel(node);
		refreshPanel();
	}
}
