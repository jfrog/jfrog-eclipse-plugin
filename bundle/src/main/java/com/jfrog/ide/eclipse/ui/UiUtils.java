package com.jfrog.ide.eclipse.ui;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.LayoutConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * @author yahavi
 */
public class UiUtils {

	public static void setGridLayout(Composite composite, int numColumns, boolean makeColumnsEqualWidth) {
		composite.setLayout(GridLayoutFactory.fillDefaults().numColumns(numColumns).equalWidth(makeColumnsEqualWidth)
				.spacing(LayoutConstants.getSpacing().x, 0).create());
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
