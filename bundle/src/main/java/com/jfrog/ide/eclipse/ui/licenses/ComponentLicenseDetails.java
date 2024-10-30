package com.jfrog.ide.eclipse.ui.licenses;

import org.eclipse.swt.widgets.Composite;
import org.jfrog.build.extractor.scan.DependencyTree;
import com.jfrog.ide.eclipse.ui.ComponentDetails;

/**
 * @author yahavi
 */
public class ComponentLicenseDetails extends ComponentDetails {
	
	private static ComponentLicenseDetails instance;
	
	public static ComponentLicenseDetails createComponentLicenseDetails(Composite parent) {
		instance = new ComponentLicenseDetails(parent);
		return instance;
	}
	
	public static ComponentLicenseDetails getInstance() {
		return instance;
	}
	
	private ComponentLicenseDetails(Composite parent) {
		super(parent, "Details");
	}

	@Override
	public void createDetailsView(DependencyTree node) {
		createCommonInfo(node);
		refreshPanel();
	}
	
	public static void disposeComponentDetails() {
		if (instance != null) {
			instance.dispose();
		}
	}
}
