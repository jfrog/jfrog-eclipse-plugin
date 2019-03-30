package com.jfrog.ide.eclipse.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import com.jfrog.ide.eclipse.ui.actions.CollapseAll;
import com.jfrog.ide.eclipse.ui.actions.ExpandAll;
import com.jfrog.ide.eclipse.ui.actions.Filter;
import com.jfrog.ide.eclipse.ui.actions.Refresh;

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
