package org.jfrog.eclipse.ui.licenses;

import org.eclipse.swt.widgets.Composite;
import org.jfrog.build.extractor.scan.DependenciesTree;
import org.jfrog.eclipse.ui.ComponentDetails;

/**
 * @author yahavi
 */
public class ComponentLicenseDetails extends ComponentDetails {
	
	private static ComponentLicenseDetails instance;
	
	public static ComponentLicenseDetails create(Composite parent) {
		instance = new ComponentLicenseDetails(parent);
		return instance;
	}
	
	public static ComponentLicenseDetails get() {
		return instance;
	}
	
	private ComponentLicenseDetails(Composite parent) {
		super(parent, "Details");
	}

	@Override
	public void createDetailsView(DependenciesTree node) {
		createCommonInfo(node);
		refreshPanel();
	}
	
	public static void disposeComponentDetails() {
		if (instance != null) {
			instance.dispose();
		}
	}
}
