package org.jfrog.eclipse.ui.actions;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.ISharedImages;
import org.jfrog.eclipse.configuration.XrayServerConfigImpl;
import org.jfrog.eclipse.log.Logger;
import org.jfrog.eclipse.scan.ScanManagersFactory;

/**
 * @author yahavi
 */
public class Refresh extends Action {

	public Refresh(ToolBar toolBar) {
		super(toolBar, ISharedImages.IMG_ELCL_SYNCED);
	}

	@Override
	public void execute(SelectionEvent event) {
		if (!XrayServerConfigImpl.getInstance().areCredentialsSet()) {
			Logger.getLogger().error("Xray server is not configured.");
			return;
		}
		ScanManagersFactory.getInstance().startScan(getParent(), false);
	}
}
