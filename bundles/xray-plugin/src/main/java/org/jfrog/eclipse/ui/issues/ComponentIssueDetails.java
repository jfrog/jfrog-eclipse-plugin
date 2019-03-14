package org.jfrog.eclipse.ui.issues;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.widgets.Composite;
import org.jfrog.build.extractor.scan.DependenciesTree;
import org.jfrog.build.extractor.scan.Issue;
import org.jfrog.eclipse.ui.ComponentDetails;

/**
 * @author yahavi
 */
public class ComponentIssueDetails extends ComponentDetails {

	public ComponentIssueDetails(Composite parent) {
		super(parent, "Component Details");
	}

	@Override
	public void createDetailsView(DependenciesTree node) {
		createCommonPanel(node);
		Issue topIssue = node.getTopIssue();
		addSection("Top Issue Severity:", StringUtils.capitalize(topIssue.getSeverity().toString()));
		addSection("Top Issue Type:", StringUtils.capitalize(topIssue.getIssueType()));
		addSection("Issues Count:", String.valueOf(node.getIssueCount()));
		refreshPanel();
	}
}
