package com.jfrog.ide.eclipse.configuration;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import com.jfrog.ide.eclipse.scan.ScanManagersFactory;
import com.jfrog.ide.eclipse.ui.ComponentDetails;
import com.jfrog.ide.eclipse.ui.issues.ComponentIssueDetails;
import com.jfrog.ide.eclipse.ui.licenses.ComponentLicenseDetails;

/**
 * Panel for configuring Xray URL, username and password.
 * 
 * @author yahavi
 */
public class XrayGlobalConfiguration extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public XrayGlobalConfiguration() {
		super(GRID);
	}

	@Override
	public void createFieldEditors() {
		StringFieldEditor urlEditor = new StringFieldEditor(PreferenceConstants.XRAY_URL, "URL:",
				getFieldEditorParent());
		StringFieldEditor usernameEditor = new StringFieldEditor(PreferenceConstants.XRAY_USERNAME, "Username:",
				getFieldEditorParent());
		StringFieldEditor passwordEditor = new StringFieldEditor(PreferenceConstants.XRAY_PASSWORD, "Password:",
				getFieldEditorParent());
		passwordEditor.getTextControl(getFieldEditorParent()).setEchoChar('*');

		addField(urlEditor);
		addField(usernameEditor);
		addField(passwordEditor);
		addField(new TestConnectionButton(urlEditor, usernameEditor, passwordEditor, getFieldEditorParent()));
	}

	@Override
	public boolean performOk() {
		super.performOk();
		if (!XrayServerConfigImpl.getInstance().areCredentialsSet()) {
			return true;
		}
			CliDriverWrapper.getInstance().getCliDriver().addCliServerConfig(
			XrayServerConfigImpl.getInstance().getXrayUrl(),
			XrayServerConfigImpl.getInstance().getArtifactoryUrl(),
			CliDriverWrapper.getInstance().CLIENT_ID_SERVER,
			XrayServerConfigImpl.getInstance().getUsername(),
			XrayServerConfigImpl.getInstance().getPassword(),
			XrayServerConfigImpl.getInstance().getAccessToken(),
			Paths.get(System.getProperty("user.home"), ".jfrog-eclipse-plugin").toFile()  // Convert Path to File here
		);
		boolean doQuickScan = false;
		ComponentDetails[] componentsDetails = { ComponentIssueDetails.getInstance(), ComponentLicenseDetails.getInstance() };
		for (ComponentDetails componentsDetail : componentsDetails) {
			if (componentsDetail != null) {
				componentsDetail.credentialsSet();
				doQuickScan = true;
			}
		}
		if (doQuickScan) {
			ScanManagersFactory.getInstance().startScan(getShell().getParent(), true);
		}
		return true;
	}

	@Override
	public void init(IWorkbench workbench) {
		setDescription("JFrog Xray credentials:");
		setPreferenceStore(new ScopedPreferenceStore(ConfigurationScope.INSTANCE, PreferenceConstants.XRAY_QUALIFIER));
	}

	public static PreferenceDialog createPreferenceDialog() {
		return PreferencesUtil.createPreferenceDialogOn(Display.getCurrent().getActiveShell(),
				"com.jfrog.ide.eclipse.ui.preferences.XrayServerConfig",
				new String[] { "com.jfrog.ide.eclipse.ui.preferences.XrayServerConfig" }, null);
	}

}
