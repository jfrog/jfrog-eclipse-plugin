package com.jfrog.ide.eclipse.configuration;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.ICoreRunnable;
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
import org.jfrog.build.extractor.executor.CommandResults;

import com.jfrog.ide.common.configuration.JfrogCliDriver;
import com.jfrog.ide.eclipse.log.Logger;
import com.jfrog.ide.eclipse.scheduling.CliJob;
import com.jfrog.ide.eclipse.ui.ComponentDetails;
import com.jfrog.ide.eclipse.ui.issues.ComponentIssueDetails;
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
		addField(new BooleanFieldEditor(PreferenceConstants.DEBUG_LOGS, "Generate Debug Logs", getFieldEditorParent()));
	}

	@Override
	public boolean performOk() {
		super.performOk();
		if (!XrayServerConfigImpl.getInstance().areCredentialsSet()) {
			return true;
		}
		
		final Map<String, String> configEnv;
		
		// define log level
		if (XrayServerConfigImpl.getInstance().getIsDebugLogs()) {
			Logger.getInstance().setLogLevel(Logger.DEBUG);
			configEnv = PreferenceConstants.getCliDebugLogsEnvVars();
		} else {
			Logger.getInstance().setLogLevel(Logger.INFO);
			configEnv = new HashMap<>();
		}

	    // Define the runnable to execute the CLI config command 
	    ICoreRunnable runnableServerConfig = monitor -> {
	        try {
	            JfrogCliDriver cliDriver = CliDriverWrapper.getInstance().getCliDriver();
        		CommandResults configResults = cliDriver.addCliServerConfig(
	                XrayServerConfigImpl.getInstance().getXrayUrl(),
	                XrayServerConfigImpl.getInstance().getArtifactoryUrl(),
	                CliDriverWrapper.CLIENT_ID_SERVER,
	                XrayServerConfigImpl.getInstance().getUsername(),
	                XrayServerConfigImpl.getInstance().getPassword(),
	                XrayServerConfigImpl.getInstance().getAccessToken(),
	                CliDriverWrapper.HOME_PATH.toFile(),
	                configEnv
	            );
	            if (!configResults.getErr().isBlank()) {
	            	throw new Exception(configResults.getErr());
	            }
	        } catch (Exception e) {
	            CliDriverWrapper.getInstance().showCliError("An error occurred while setting up the server connection:", e);
	        }
	    };

	    // Schedule the CliJob to execute the runnable
	    CliJob.doSchedule("Setup Server Configuration", runnableServerConfig);
	    
		ComponentDetails[] componentsDetails = { ComponentIssueDetails.getInstance()};
		for (ComponentDetails componentsDetail : componentsDetails) {
			if (componentsDetail != null) {
				componentsDetail.credentialsSet();
			}
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
