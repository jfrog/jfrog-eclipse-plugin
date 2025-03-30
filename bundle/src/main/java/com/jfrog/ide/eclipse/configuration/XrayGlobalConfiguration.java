package com.jfrog.ide.eclipse.configuration;

import java.io.IOException;

import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import com.jfrog.ide.eclipse.log.Logger;
import com.jfrog.ide.eclipse.scan.ScanManager;
import com.jfrog.ide.eclipse.ui.ComponentDetails;
import com.jfrog.ide.eclipse.ui.issues.ComponentIssueDetails;
import com.jfrog.ide.eclipse.ui.issues.IssuesTree;

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
		
		BooleanFieldEditor debugLogsCheckbox = new BooleanFieldEditor(PreferenceConstants.DEBUG_LOGS, "Generate Debug Logs",
				getFieldEditorParent());
		addField(debugLogsCheckbox);
	}

	@Override
	public boolean performOk() {
		// TODO: This code runs when clicking the 'Apply' button in the settings panel. Implement server configuration using CliDriver here
		super.performOk();
		if (!XrayServerConfigImpl.getInstance().areCredentialsSet()) {
			return true;
		}
		boolean doQuickScan = false;
		ComponentDetails[] componentsDetails = { ComponentIssueDetails.getInstance()};
		for (ComponentDetails componentsDetail : componentsDetails) {
			if (componentsDetail != null) {
				componentsDetail.credentialsSet();
				doQuickScan = true;
			}
		}
		if (doQuickScan) {
			ScanManager.getInstance().startScan(getShell().getParent(),
						getPreferenceStore().getBoolean(PreferenceConstants.DEBUG_LOGS));
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
