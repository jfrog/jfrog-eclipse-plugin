package com.jfrog.ide.eclipse.configuration;

import java.io.IOException;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.osgi.framework.FrameworkUtil;

import com.jfrog.ide.common.utils.XrayConnectionUtils;
import com.jfrog.xray.client.Xray;
import com.jfrog.xray.client.impl.XrayClientBuilder;
import com.jfrog.xray.client.services.system.Version;
import com.jfrog.ide.eclipse.log.Logger;

/**
 * Button in the configuration panel for testing connection with Xray.
 * 
 * @author yahavi
 */
public class TestConnectionButton extends FieldEditor {

	private static final String USER_AGENT = "jfrog-eclipse-plugin/"
			+ FrameworkUtil.getBundle(XrayGlobalConfiguration.class).getVersion().toString();
	private StringFieldEditor urlEditor, usernameEditor, passwordEditor;
	private Label connectionResults;
	private Button button;

	public TestConnectionButton(StringFieldEditor urlEditor, StringFieldEditor usernameEditor,
			StringFieldEditor passwordEditor, Composite parent) {
		super("", "Test Connection", parent);
		this.urlEditor = urlEditor;
		this.usernameEditor = usernameEditor;
		this.passwordEditor = passwordEditor;
		createButton(parent);
		createConnectionResults(parent);
	}

	private void createButton(Composite parent) {
		button = new Button(parent, SWT.PUSH);
		button.setText(getLabelText());
		button.addSelectionListener(new ButtonSelection());
		button.setLayoutData(GridDataFactory.swtDefaults().align(SWT.LEFT, SWT.TOP).create());
	}

	private void createConnectionResults(Composite parent) {
		connectionResults = new Label(parent, SWT.WRAP);
		connectionResults.setLayoutData(GridDataFactory.fillDefaults().grab(false, true).create());
	}

	@Override
	protected void doFillIntoGrid(Composite parent, int numColumns) {
	}

	@Override
	protected void adjustForNumColumns(int numColumns) {
	}

	@Override
	protected void doLoad() {
	}

	@Override
	protected void doLoadDefault() {
	}

	@Override
	protected void doStore() {
	}

	@Override
	public int getNumberOfControls() {
		return 1;
	}
	
    private Xray createXrayClient() {
    	String url = urlEditor.getStringValue();
    	String xrayUrl = url.endsWith("/") ? url + "xray" : url + "/xray";
    	XrayServerConfigImpl serverConfig = XrayServerConfigImpl.getInstance();
    	
    	return (Xray) new XrayClientBuilder()
                .setUrl(xrayUrl)
                .setUserName(usernameEditor.getStringValue())
                .setPassword(passwordEditor.getStringValue())
                .setUserAgent(USER_AGENT)
                .setInsecureTls(false)
                .setSslContext(serverConfig.getSslContext())
                .setProxyConfiguration(serverConfig.getProxyConfForTargetUrl(url))
                .setLog(Logger.getInstance()) 
                .build();
    }

	private class ButtonSelection extends SelectionAdapter {
		@Override
		public void widgetSelected(SelectionEvent e) {
			try {
				connectionResults.setText("Connecting to Xray...");
				
				Xray xrayClient = createXrayClient();
				Version xrayVersion = xrayClient.system().version();

				if (!XrayConnectionUtils.isXrayVersionSupported(xrayVersion)) {
					connectionResults.setText(XrayConnectionUtils.Results.unsupported(xrayVersion));
				} else {
					Pair<Boolean, String> testComponentPermissionRes = XrayConnectionUtils
							.testComponentPermission(xrayClient);
					if (!testComponentPermissionRes.getLeft()) {
						throw new IOException(testComponentPermissionRes.getRight());
					}
					connectionResults.setText(XrayConnectionUtils.Results.success(xrayVersion));
				}
			} catch (IOException | IllegalArgumentException exeption) {
				connectionResults.setText(XrayConnectionUtils.Results.error(exeption));
			}
		}
	}
}
