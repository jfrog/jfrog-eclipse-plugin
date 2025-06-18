package com.jfrog.ide.eclipse.ui;

import static com.jfrog.ide.eclipse.ui.UiUtils.createLabel;
import static com.jfrog.ide.eclipse.ui.UiUtils.setGridLayout;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;

import com.jfrog.ide.common.nodes.FileIssueNode;
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
		recreateComponentDetails();
	}

	public abstract void createDetailsView(FileIssueNode node);
	
	public void recreateComponentDetails() {
		if (isDisposed()) {
			return;
		}
		for (Control control : getChildren()) {
			control.dispose();
		}
		if (!XrayServerConfigImpl.getInstance().areCredentialsSet()) {
			createMissingCredentialsPanel();
			return;
		}
		createComponentsPanel();
		refreshPanel();
	}

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
		recreateComponentDetails();
	}

	protected void createComponentsPanel() {
		createLabel(this, title);
		scrolledComposite = new ScrolledComposite(this, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.FILL);
		scrolledComposite.setBackground(getBackground());
		setGridLayout(scrolledComposite, 1, false);
		componentDetailsPanel = new Panel(scrolledComposite);
		componentDetailsPanel.setBackground(scrolledComposite.getBackground());
		setGridLayout(componentDetailsPanel, 2, false);
		UiUtils.createDisabledTextLabel(componentDetailsPanel, "Issue information is not available");
		scrolledComposite.setContent(componentDetailsPanel);
	}

	/**
	 * Create the common information.
	 * 
	 * @param node - Extract the component information from this node.
	 */
	protected void createCommonInfo(FileIssueNode node) {
		for (Control control : componentDetailsPanel.getChildren()) {
			control.dispose();
		}
		addSection("Title:", node.getTitle());
		addSection("Reporter:", node.getReporterType().getScannerName());
		addSection("Severity:", node.getSeverity().getSeverityName());
		addSection("Full Description:", node.getFullDescription());
		addSection("File Path:", node.getFilePath());
		addSection("Line Snippet:", node.getLineSnippet());

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

	protected abstract void createBrowserJCEF(Composite parent);
}
