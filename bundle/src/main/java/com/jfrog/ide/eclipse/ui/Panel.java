package com.jfrog.ide.eclipse.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.Composite;

/**
 * @author yahavi
 */
public class Panel extends Composite {

	public Panel(Composite parent) {
		super(parent, SWT.NONE);
	}

	public Panel(Panel parent) {
		this((Composite) parent);
	}

	public Panel(CTabFolder parent) {
		this((Composite) parent);
	}

}
