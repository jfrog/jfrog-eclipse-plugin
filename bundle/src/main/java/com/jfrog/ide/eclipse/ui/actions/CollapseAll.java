package com.jfrog.ide.eclipse.ui.actions;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.ToolBar;
import com.jfrog.ide.eclipse.ui.issues.IssuesTree;

/**
 * Collapse the issues tree.
 * 
 * @author yahavi
 */
public class CollapseAll extends Action {

	public CollapseAll(ToolBar toolBar) {
		super(toolBar, "Collapse All", "collapseAll");
	}

	@Override
	public void execute(SelectionEvent event) {
		IssuesTree.getInstance().collapseAll();
	}
}
