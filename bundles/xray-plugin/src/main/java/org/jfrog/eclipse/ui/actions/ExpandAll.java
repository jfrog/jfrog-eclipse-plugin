package org.jfrog.eclipse.ui.actions;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.ISharedImages;
import org.jfrog.eclipse.ui.issues.IssuesTree;
import org.jfrog.eclipse.ui.licenses.LicensesTree;

/**
 * @author yahavi
 */
public class ExpandAll extends Action {

	public ExpandAll(ToolBar toolBar) {
		super(toolBar, ISharedImages.IMG_ELCL_COLLAPSEALL_DISABLED);
	}

	@Override
	public void execute(SelectionEvent event) {
		IssuesTree.getIssuesTree().expandAll();
		LicensesTree.getLicensesTree().expandAll();
	}
}
