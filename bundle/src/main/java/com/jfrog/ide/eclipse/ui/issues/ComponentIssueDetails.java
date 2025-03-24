package com.jfrog.ide.eclipse.ui.issues;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.widgets.Composite;
import org.jfrog.build.extractor.scan.DependencyTree;
import org.jfrog.build.extractor.scan.Issue;
import com.jfrog.ide.eclipse.ui.ComponentDetails;

/**
 * @author yahavi
 */
public class ComponentIssueDetails extends ComponentDetails {

	private static ComponentIssueDetails instance;

	public static ComponentIssueDetails createComponentIssueDetails(Composite parent) {
		instance = new ComponentIssueDetails(parent);
		return instance;
	}

	public static ComponentIssueDetails getInstance() {
		return instance;
	}

	private ComponentIssueDetails(Composite parent) {
		super(parent, "Component Details");
	}

	@Override
	public void createDetailsView(DependencyTree node) {
		createCommonInfo(node);
		Issue topIssue = node.getTopIssue();
		addSection("Top Issue Severity:", StringUtils.capitalize(topIssue.getSeverity().toString()));
		addSection("Issues Count:", String.valueOf(node.getIssueCount()));
		refreshPanel();
	}

	public static void disposeComponentDetails() {
		if (instance != null) {
			instance.dispose();
		}
	}
}
