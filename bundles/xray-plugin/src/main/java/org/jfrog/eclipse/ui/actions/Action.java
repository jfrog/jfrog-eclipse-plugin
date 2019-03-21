package org.jfrog.eclipse.ui.actions;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.jfrog.eclipse.ui.IconManager;

/**
 * Base class for items in the tool bar.
 * 
 * @author yahavi
 */
public abstract class Action extends ToolItem {

	public Action(ToolBar toolBar, String image) {
		super(toolBar, SWT.PUSH);
		setImage(IconManager.load(image));
		addSelectionListener(new ActionPerformed());
	}

	public abstract void execute(SelectionEvent e);

	/**
	 * Override to allow inheritance
	 */
	@Override
	protected void checkSubclass() {
	}

	private class ActionPerformed extends SelectionAdapter {

		@Override
		public void widgetSelected(SelectionEvent e) {
			execute(e);
		}
	}
}
