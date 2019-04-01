package com.jfrog.ide.eclipse.ui;

import java.util.List;

import org.apache.commons.lang3.mutable.MutableBoolean;
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
 * Base filter menu.
 * 
 * @author yahavi
 */
public abstract class FilterDialog extends Dialog {

	protected List<Button> buttons = Lists.newArrayList();
	protected Button selectAllButton;
	private MutableBoolean selectAllState;
	private Font titleFont;
	private String title;

	/**
	 * Construct a filter dialog.
	 * 
	 * @param parentShell    - The shell that will contain the dialog.
	 * @param title          - Filter dialog title - "Severity" or "License".
	 * @param selectAllState - The state of "All" checkbox.
	 */
	public FilterDialog(Shell parentShell, String title, MutableBoolean selectAllState) {
		super(parentShell);
		this.title = title;
		this.selectAllState = selectAllState;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		createTitle(container);
		selectAllButton = new Button(container, SWT.CHECK);
		selectAllButton.setText("All");
		selectAllButton.setSelection(selectAllState.booleanValue());
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

	/**
	 * Don't create the button bar.
	 */
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

	/**
	 * Logic of the select all button.
	 */
	protected abstract void selectAll();

	private class SelectAllListener extends SelectionAdapter {

		@Override
		public void widgetSelected(SelectionEvent event) {
			super.widgetSelected(event);
			buttons.forEach(button -> button.setSelection(selectAllButton.getSelection()));
			selectAllState.setValue(selectAllButton.getSelection());
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
