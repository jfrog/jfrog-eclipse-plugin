package com.jfrog.ide.eclipse.ui.issues;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.jfrog.build.extractor.scan.DependencyTree;
import org.jfrog.build.extractor.scan.Severity;
import com.jfrog.ide.eclipse.ui.IconManager;

/**
 * Show severity icons in the issues tree.
 * 
 * @author yahavi
 */
public class IssuesTreeColumnLabelProvider extends ColumnLabelProvider {

	@Override
	public Image getImage(Object element) {
		DependencyTree scanTreeNode = (DependencyTree) element;
		Severity severity = scanTreeNode.getTopIssue().getSeverity();
		return IconManager.load(severity.name().toLowerCase());
	}
}
