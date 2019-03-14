package org.jfrog.eclipse.ui.issues;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.jfrog.build.extractor.scan.DependenciesTree;
import org.jfrog.build.extractor.scan.Severity;
import org.jfrog.eclipse.ui.IconManager;

/**
 * @author yahavi
 */
public class IssuesTreeColumnLabelProvider extends ColumnLabelProvider {

	@Override
	public Image getImage(Object element) {
		DependenciesTree scanTreeNode = (DependenciesTree) element;
		Severity severity = scanTreeNode.getTopIssue().getSeverity();
		return IconManager.load(severity.name().toLowerCase());
	}
}
