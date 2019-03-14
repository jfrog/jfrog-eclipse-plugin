package org.jfrog.eclipse.ui;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.google.common.collect.Lists;

/**
 * @author yahavi
 */
public abstract class FilterDialog extends Dialog {
	private String title;
	private Font titleFont;
	protected Button selectAllButton;
	protected List<Button> buttons = Lists.newArrayList();

	public FilterDialog(Shell parentShell, String title) {
		super(parentShell);
		this.title = title;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		createTitle(container);
		selectAllButton = new Button(container, SWT.CHECK);
		selectAllButton.setText("All");
		selectAllButton.setSelection(true);
		selectAllButton.addSelectionListener(new SelectAllListener());
		return container;
	}

	private void createTitle(Composite container) {
		Label label = new Label(container, SWT.NONE);
		label.setText(title);
		FontData fontData = label.getFont().getFontData()[0];
		titleFont = new Font(Display.getCurrent(), new FontData(fontData.getName(), fontData.getHeight(), SWT.BOLD));
		label.setFont(titleFont);
	}

	@Override
	protected Control createButtonBar(Composite parent) {
		return null;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(title);
	}

	@Override
	public boolean close() {
		titleFont.dispose();
		return super.close();
	}
	
	protected abstract void selectAll();
	
	private class SelectAllListener extends SelectionAdapter {

		@Override
		public void widgetSelected(SelectionEvent event) {
			super.widgetSelected(event);
			buttons.forEach(button -> button.setSelection(selectAllButton.getSelection()));
			selectAll();
		}
	}
	
	protected abstract class FilterButton extends Button {
		public FilterButton(Composite parent, String name, boolean isSelected) {
			super(parent, SWT.CHECK);
			setText(name);
			setSelection(isSelected);
		}

		/**
		 * Override to allow inheritance
		 */
		@Override
		protected void checkSubclass() {
		}
	}
}
