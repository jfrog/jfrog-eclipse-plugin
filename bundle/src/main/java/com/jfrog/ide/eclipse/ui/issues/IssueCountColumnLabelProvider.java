package com.jfrog.ide.eclipse.ui.issues;

import java.util.List;

import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;

import com.jfrog.ide.common.nodes.FileTreeNode;

/**
 * The issues count component in the issues tree.
 * 
 * @author yahavi
 */
public class IssueCountColumnLabelProvider extends ColumnLabelProvider {

	private Font font;

	public IssueCountColumnLabelProvider(Composite parent) {
		this.font = FontDescriptor.createFrom(parent.getFont()).setStyle(SWT.RIGHT).createFont(parent.getDisplay());
	}

	@Override
	public String getText(Object element) {
		int issueCount = 0;
		if (element instanceof FileTreeNode) {
			issueCount = ((FileTreeNode) element).getChildCount();
		}
		return issueCount == 0 ? "" : "(" + issueCount + ")";
	}

	@Override
	public Font getFont(Object element) {
		return font;
	}

	@Override
	public void dispose() {
		if (font != null) {
			font.dispose();
		}
		super.dispose();
	}
}
