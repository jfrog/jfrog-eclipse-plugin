package com.jfrog.ide.eclipse.ui.issues;

import org.eclipse.swt.widgets.Composite;

import com.jfrog.ide.common.nodes.FileIssueNode;
import com.jfrog.ide.common.nodes.SastIssueNode;
import com.jfrog.ide.common.nodes.ScaIssueNode;
import com.jfrog.ide.common.parse.Applicability;
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
		super(parent, "Issue Details");
	}

	@Override
	public void createDetailsView(FileIssueNode node) {
		createCommonInfo(node);
		if (node instanceof ScaIssueNode ) {
			ScaIssueNode scaNode = (ScaIssueNode) node;
			Applicability applicability = scaNode.getApplicability();
			addSection("Component Name:", scaNode.getComponentName());
			addSection("Component Version:", scaNode.getComponentVersion());
			addSection("Fixed Versions:", scaNode.getFixedVersions());
			addSection("Applicability:", applicability != null ? applicability.getValue() : "");  
		} else if (node instanceof SastIssueNode) {
			SastIssueNode sastNode = (SastIssueNode) node;
			addSection("Rule ID:", sastNode.getRuleId());
		}
		refreshPanel();
	}

	public static void disposeComponentDetails() {
		if (instance != null) {
			instance.dispose();
		}
	}
}
