package org.jfrog.eclipse.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

/**
 * @author yahavi
 */
public class UiUtils {

	public static void setGridLayout(Composite composite, int numColumns, boolean makeColumnsEqualWidth) {
		GridLayout layout = new GridLayout(numColumns, makeColumnsEqualWidth);
		layout.marginWidth = layout.marginHeight = 0;
		layout.horizontalSpacing = layout.verticalSpacing = 0;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	}

	public static void createText(Panel parent, String str) {
		Text text = new Text(parent, SWT.LEFT_TO_RIGHT);
		text.setEditable(false);
		text.setText(str);
	}

	public static void createDisabledTextLabel(Panel parent, String str) {
		Text text = new Text(parent, SWT.NONE);
		text.setEnabled(false);
		text.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		text.setText(str);
	}
}
