package com.jfrog.ide.eclipse.ui.issues;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;

import com.jfrog.ide.common.nodes.FileIssueNode;
import com.jfrog.ide.common.nodes.FileTreeNode;
import com.jfrog.ide.common.nodes.ScaIssueNode;
import com.jfrog.ide.common.nodes.subentities.Severity;
import com.jfrog.ide.common.parse.Applicability;
import com.jfrog.ide.eclipse.ui.IconManager;

/**
 * Show severity icons in the issues tree.
 * 
 * @author yahavi
 */
public class IssuesTreeColumnLabelProvider extends ColumnLabelProvider {

	@Override
	public Image getImage(Object element) {
		Severity severity = null;
		if (element instanceof FileTreeNode) {
			severity = ((FileTreeNode) element).getSeverity();
		} else if (element instanceof ScaIssueNode) {
			ScaIssueNode issueNode = (ScaIssueNode) element;
			severity = issueNode.getSeverity();
			if (Applicability.NOT_APPLICABLE.equals(issueNode.getApplicability())) {
				severity = Severity.getNotApplicableSeverity(severity);
			}
		} else if (element instanceof FileIssueNode) {
			severity = ((FileIssueNode) element).getSeverity();
		}
        return IconManager.load(severity.name().toLowerCase());
	}
}
