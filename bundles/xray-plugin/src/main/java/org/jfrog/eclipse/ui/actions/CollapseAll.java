package org.jfrog.eclipse.ui.actions;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.ISharedImages;
import org.jfrog.eclipse.ui.issues.IssuesTree;
import org.jfrog.eclipse.ui.licenses.LicensesTree;

/**
 * @author yahavi
 */
public class CollapseAll extends Action {

	public CollapseAll(ToolBar toolBar) {
		super(toolBar, ISharedImages.IMG_ELCL_COLLAPSEALL);
	}

	@Override
	public void execute(SelectionEvent event) {
		IssuesTree.getIssuesTree().collapseAll();
		LicensesTree.getLicensesTree().collapseAll();
	}
}
