package org.jfrog.eclipse.ui;

import static org.jfrog.eclipse.ui.UiUtils.createText;
import static org.jfrog.eclipse.ui.UiUtils.setGridLayout;

import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.jfrog.build.extractor.scan.DependenciesTree;
import org.jfrog.build.extractor.scan.GeneralInfo;
import org.jfrog.build.extractor.scan.License;
import org.jfrog.eclipse.configuration.XrayGlobalConfiguration;
import org.jfrog.eclipse.configuration.XrayServerConfigImpl;
import org.jfrog.utils.Utils;

/**
 * @author yahavi
 */
public abstract class ComponentDetails extends Panel {

	protected Panel componentDetailsPanel;
	private Hyperlink credentialsConfigLink;
	private String title;

	public ComponentDetails(Composite parent, String title) {
		super(parent);
		this.title = title;
		setGridLayout(this, 1, false);
		if (!XrayServerConfigImpl.getInstance().areCredentialsSet()) {
			createMissingCredentialsPanel();
			return;
		}
		createComponentsPanel();
	}

	public abstract void createDetailsView(DependenciesTree node);

	private void createMissingCredentialsPanel() {
		credentialsConfigLink = new Hyperlink(this, SWT.WRAP);
		credentialsConfigLink.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		credentialsConfigLink.setText("To start using the JFrog Plugin, please configure your JFrog Xray details.");
		credentialsConfigLink.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_BLUE));
		credentialsConfigLink.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(final HyperlinkEvent e) {
				XrayGlobalConfiguration.createPreferenceDialog().open();
			}
		});
	}
	
	public void credentialsSet() {
		if (credentialsConfigLink == null || credentialsConfigLink.isDisposed()) {
			return;
		}
		credentialsConfigLink.dispose();
		createComponentsPanel();
		refreshPanel();
	}

	protected void createComponentsPanel() {
		createText(this, title);
		componentDetailsPanel = new Panel(this);
		setGridLayout(componentDetailsPanel, 2, false);
		UiUtils.createDisabledTextLabel(componentDetailsPanel, "Component information is not available");
	}

	protected void createCommonPanel(DependenciesTree node) {
		for (Control control : componentDetailsPanel.getChildren()) {
			control.dispose();
		}
		GeneralInfo generalInfo = ObjectUtils.defaultIfNull(node.getGeneralInfo(), new GeneralInfo());
		if (!StringUtils.equalsIgnoreCase("Npm", generalInfo.getPkgType())) {
			addSection("Group:", generalInfo.getGroupId());
		}

		addSection("Artifact:", generalInfo.getArtifactId());
		addSection("Version:", generalInfo.getVersion());
		addSection("Type:", StringUtils.capitalize(generalInfo.getPkgType()));
		addSection("Path:", generalInfo.getPath());
		addLicenses(node.getLicenses());
	}

	protected void addSection(String name, String content) {
		if (StringUtils.isBlank(content)) {
			return;
		}
		createText(componentDetailsPanel, name);
		createText(componentDetailsPanel, content);
	}

	protected void refreshPanel() {
		componentDetailsPanel.pack();
		componentDetailsPanel.getParent().layout();
	}

	private void addLicenses(Set<License> licenses) {
		if (licenses.isEmpty()) {
			return;
		}
		createText(componentDetailsPanel, "Licenses");
		Panel licensesPanel = new Panel(componentDetailsPanel);
		licensesPanel.setLayout(new FillLayout());
		licenses.forEach(license -> {
			if (CollectionUtils.isEmpty(license.getMoreInfoUrl())) {
				createText(licensesPanel, Utils.createLicenseString(license));
			} else {
				addHyperlink(licensesPanel, Utils.createLicenseString(license), license.getMoreInfoUrl().get(0));
			}
		});
	}

	private static void addHyperlink(Panel parent, String text, String url) {
		Hyperlink link = new Hyperlink(parent, SWT.WRAP);
		link.setText(text);
		link.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_BLUE));
		link.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(final HyperlinkEvent e) {
				Program.launch(url);
			}
		});
	}
}
