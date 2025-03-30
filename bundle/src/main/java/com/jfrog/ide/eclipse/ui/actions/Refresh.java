package com.jfrog.ide.eclipse.ui.actions;

import java.io.IOException;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.ToolBar;

import com.jfrog.ide.eclipse.configuration.PreferenceConstants;
import com.jfrog.ide.eclipse.configuration.XrayServerConfigImpl;
import com.jfrog.ide.eclipse.log.Logger;
import com.jfrog.ide.eclipse.scan.ScanManager;

/**
 * Start a new slow scan.
 * 
 * @author yahavi
 */
public class Refresh extends Action {

	public Refresh(ToolBar toolBar) {
		super(toolBar, "Refresh", "refresh");
	}

	@Override
	public void execute(SelectionEvent event) {
		if (!XrayServerConfigImpl.getInstance().areCredentialsSet()) {
			Logger.getInstance().error("Xray server is not configured.");
			return;
		}
		
		try {
			ScanManager.getInstance().startScan(getParent(), XrayServerConfigImpl.getInstance().getIsDebugLogs());
		} catch (Exception e) {
			// TODO: pop an error message window
			Logger.getInstance().error(e.getMessage());
		}
	}
		
}
