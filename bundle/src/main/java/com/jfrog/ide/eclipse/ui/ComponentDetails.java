package com.jfrog.ide.eclipse.ui;

import static com.jfrog.ide.eclipse.ui.UiUtils.createLabel;
import static com.jfrog.ide.eclipse.ui.UiUtils.setGridLayout;

import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.jfrog.build.extractor.scan.DependenciesTree;
import org.jfrog.build.extractor.scan.GeneralInfo;
import org.jfrog.build.extractor.scan.License;

import com.jfrog.ide.common.utils.Utils;
import com.jfrog.ide.eclipse.configuration.XrayGlobalConfiguration;
import com.jfrog.ide.eclipse.configuration.XrayServerConfigImpl;

/**
 * Base class for ComponentDetails panels. Those panels contain information on a
 * single component.
 * 
 * @author yahavi
 */
public abstract class ComponentDetails extends Panel {

	protected Composite componentDetailsPanel;
	private ScrolledComposite scrolledComposite;
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

	/**
	 * Create this panel if there are missing credentials.
	 */
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

	/**
	 * Will be called after credentials were set.
	 */
	public void credentialsSet() {
		if (credentialsConfigLink == null || credentialsConfigLink.isDisposed()) {
			return;
		}
		credentialsConfigLink.dispose();
		createComponentsPanel();
		refreshPanel();
	}

	protected void createComponentsPanel() {
		createLabel(this, title);
		scrolledComposite = new ScrolledComposite(this, SWT.BORDER | SWT.V_SCROLL | SWT.FILL);
		scrolledComposite.setBackground(getBackground());
		setGridLayout(scrolledComposite, 1, false);
		componentDetailsPanel = new Panel(scrolledComposite);
		componentDetailsPanel.setBackground(scrolledComposite.getBackground());
		setGridLayout(componentDetailsPanel, 2, false);
		UiUtils.createDisabledTextLabel(componentDetailsPanel, "Component information is not available");
		scrolledComposite.setContent(componentDetailsPanel);
	}

	/**
	 * Create the common information between issues and licenses tabs.
	 * 
	 * @param node - Extract the component information from this node.
	 */
	protected void createCommonInfo(DependenciesTree node) {
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
		refreshPanel();
	}

	protected void addSection(String name, String content) {
		if (StringUtils.isBlank(content)) {
			return;
		}
		createLabel(componentDetailsPanel, name);
		createLabel(componentDetailsPanel, content);
	}

	/**
	 * Optimize component panel size.
	 */
	protected void refreshPanel() {
		layout(true, true);
		componentDetailsPanel.pack();
	}

	/**
	 * Add licenses to panel.
	 * 
	 * @param licenses - The licenses to add.
	 */
	private void addLicenses(Set<License> licenses) {
		if (licenses.isEmpty()) {
			return;
		}
		createLabel(componentDetailsPanel, "Licenses:");
		Panel licensesPanel = new Panel(componentDetailsPanel);
		licensesPanel.setLayout(GridLayoutFactory.fillDefaults().spacing(0, 0).create());
		licenses.forEach(license -> {
			if (CollectionUtils.isEmpty(license.getMoreInfoUrl())) {
				// Add a license without URL.
				createLabel(licensesPanel, Utils.createLicenseString(license));
			} else {
				// Add a license with URL.
				addHyperlink(licensesPanel, Utils.createLicenseString(license), license.getMoreInfoUrl().get(0));
			}
		});
	}

	private static void addHyperlink(Panel parent, String text, String url) {
		Link link = new Link(parent, SWT.NONE);
		link.setText("<A>" + text + "</A>");
		link.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Program.launch(url);
			}
		});
	}
}
