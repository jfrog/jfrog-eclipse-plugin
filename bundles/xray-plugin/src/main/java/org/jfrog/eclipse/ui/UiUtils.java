package org.jfrog.eclipse.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
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

	public static Label createLabel(Composite parent, String str) {
		Label label = new Label(parent, SWT.LEFT_TO_RIGHT);
		label.setText(str);
		return label;
	}

	public static void createDisabledTextLabel(Composite parent, String str) {
		Label text = new Label(parent, SWT.CENTER);
		text.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		text.setText(str);
	}
}
