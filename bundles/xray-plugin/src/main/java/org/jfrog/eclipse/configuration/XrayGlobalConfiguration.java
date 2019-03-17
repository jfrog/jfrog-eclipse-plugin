package org.jfrog.eclipse.configuration;

import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.jfrog.eclipse.scan.ScanManagersFactory;
import org.jfrog.eclipse.ui.ComponentDetails;
import org.jfrog.eclipse.ui.issues.ComponentIssueDetails;
import org.jfrog.eclipse.ui.licenses.ComponentLicenseDetails;

/**
 * This class represents a preference page that is contributed to the
 * Preferences dialog. By subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows us to create a page
 * that is small and knows how to save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the
 * preference store that belongs to the main plug-in class. That way,
 * preferences can be accessed directly via the preference store.
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
		boolean doQuickScan = false;
		ComponentDetails[] componentsDetails = { ComponentIssueDetails.get(), ComponentLicenseDetails.get() };
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
				"org.jfrog.eclipse.ui.preferences.XrayServerConfig",
				new String[] { "org.jfrog.eclipse.ui.preferences.XrayServerConfig" }, null);
	}

}