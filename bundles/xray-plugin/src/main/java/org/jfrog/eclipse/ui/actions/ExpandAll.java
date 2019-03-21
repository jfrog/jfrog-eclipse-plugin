package org.jfrog.eclipse.ui.actions;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.ToolBar;
import org.jfrog.eclipse.ui.issues.IssuesTree;
import org.jfrog.eclipse.ui.licenses.LicensesTree;

/**
 * @author yahavi
 */
public class ExpandAll extends Action {

	public ExpandAll(ToolBar toolBar) {
		super(toolBar, "expandAll");
	}

	@Override
	public void execute(SelectionEvent event) {
		IssuesTree.getIssuesTree().expandAll();
		LicensesTree.getLicensesTree().expandAll();
	}
}
