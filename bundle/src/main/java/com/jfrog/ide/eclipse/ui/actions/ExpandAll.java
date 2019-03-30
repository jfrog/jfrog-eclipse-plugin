package com.jfrog.ide.eclipse.ui.actions;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.ToolBar;
import com.jfrog.ide.eclipse.ui.issues.IssuesTree;
import com.jfrog.ide.eclipse.ui.licenses.LicensesTree;

/**
 * Expand the issue/licenses tree.
 * 
 * @author yahavi
 */
public class ExpandAll extends Action {

	public ExpandAll(ToolBar toolBar) {
		super(toolBar, "Expand All", "expandAll");
	}

	@Override
	public void execute(SelectionEvent event) {
		IssuesTree.getIssuesTree().expandAll();
		LicensesTree.getLicensesTree().expandAll();
	}
}
