package com.jfrog.ide.eclipse.ui.actions;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.ToolBar;
import com.jfrog.ide.eclipse.configuration.XrayServerConfigImpl;
import com.jfrog.ide.eclipse.log.Logger;
import com.jfrog.ide.eclipse.scan.ScanManagersFactory;

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
		ScanManagersFactory.getInstance().startScan(getParent(), false);
	}
}
