package org.jfrog.eclipse.ui.issues;

import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.jfrog.build.extractor.scan.DependenciesTree;

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
		int issueCount = ((DependenciesTree) element).getIssueCount();
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
