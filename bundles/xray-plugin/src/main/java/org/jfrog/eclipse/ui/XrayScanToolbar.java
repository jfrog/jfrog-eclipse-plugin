package org.jfrog.eclipse.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.jfrog.eclipse.ui.actions.CollapseAll;
import org.jfrog.eclipse.ui.actions.ExpandAll;
import org.jfrog.eclipse.ui.actions.Filter;
import org.jfrog.eclipse.ui.actions.Refresh;

/**
 * @author yahavi
 */
public class XrayScanToolbar extends Panel {

	private Filter filter;
	
	public XrayScanToolbar(Composite parent) {
		super(parent);
		ToolBar toolBar = new ToolBar(this, SWT.NONE);
		new Refresh(toolBar);
		new CollapseAll(toolBar);
		new ExpandAll(toolBar);
		filter = new Filter(toolBar);
		toolBar.pack();
	}

	public void setFilterType(Filter.FilterType filterType) {
		filter.setFilterType(filterType);
	}
}
