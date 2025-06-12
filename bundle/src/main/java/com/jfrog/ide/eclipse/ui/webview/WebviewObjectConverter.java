package com.jfrog.ide.eclipse.ui.webview;

import com.jfrog.ide.common.nodes.*;
import com.jfrog.ide.common.nodes.subentities.*;
import com.jfrog.ide.common.parse.Applicability;
import com.jfrog.ide.common.webview.ApplicableDetails;
import com.jfrog.ide.common.webview.Cve;
import com.jfrog.ide.common.webview.DependencyPage;
import com.jfrog.ide.common.webview.ImpactGraph;
import com.jfrog.ide.common.webview.ImpactGraphNode;
import com.jfrog.ide.common.webview.IssuePage;
import com.jfrog.ide.common.webview.Location;
import com.jfrog.ide.common.webview.SastIssuePage;

import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class WebviewObjectConverter {
	public static final int IMPACT_PATHS_LIMIT = 20;
    public static DependencyPage convertScaIssueToDepPage(ScaIssueNode scaNode) {
        return new DependencyPage()
				.cve(new Cve(scaNode.getTitle(), null, null, null, null, new ApplicableDetails(Applicability.getWebviewIconName(scaNode.getApplicability()), null, null)))
				.component(scaNode.getComponentName())
				.version(scaNode.getComponentVersion())
				.severity(scaNode.getSeverity().getSeverityName())
				.fixedVersion(scaNode.getFixedVersions())
				.summary(scaNode.getFullDescription())
				.impactGraph(toImpactGraph(scaNode.getImpactPaths()));
    }

    public static IssuePage convertFileIssueToIssuePage(FileIssueNode fileIssueNode) {
        return new IssuePage()
                .header(fileIssueNode.getTitle())
                .type(ConvertPageType(fileIssueNode.getReporterType()))
                .severity(fileIssueNode.getSeverity().name())
                .description(fileIssueNode.getFullDescription())
                .location(convertFileLocation(fileIssueNode));
    }

    public static IssuePage convertSastIssueToSastIssuePage(SastIssueNode sastIssueNode) {
        return new SastIssuePage(convertFileIssueToIssuePage(sastIssueNode))
                .setAnalysisSteps(convertCodeFlowsToLocations(sastIssueNode.getCodeFlows()))
                .setRuleID(sastIssueNode.getRuleId());
    }

    private static Location[] convertCodeFlowsToLocations(FindingInfo[][] codeFlows) {
        if (codeFlows != null && codeFlows.length > 0) {
            Location[] locations = new Location[codeFlows[0].length];
            for (int i = 0; i < codeFlows[0].length; i++) {
                FindingInfo codeFlow = codeFlows[0][i];
                locations[i] = new Location(
                        codeFlow.getFilePath(),
                        Paths.get(codeFlow.getFilePath()).getFileName().toString(),
                        codeFlow.getRowStart(),
                        codeFlow.getColStart(),
                        codeFlow.getRowEnd(),
                        codeFlow.getColEnd(),
                        codeFlow.getLineSnippet());
            }
            return locations;
        }
        return null;
    }

    private static String ConvertPageType(SourceCodeScanType reporterType) {
        return switch (reporterType) {
            case SECRETS -> "SECRETS";
            case IAC -> "IAC";
            case SAST -> "SAST";
            default -> "EMPTY";
        };
    }

    private static Location convertFileLocation(FileIssueNode fileIssueNodeNode) {
        return new Location(
                fileIssueNodeNode.getFilePath(),
                Paths.get(fileIssueNodeNode.getFilePath()).getFileName().toString(),
                fileIssueNodeNode.getRowStart() + 1,
                fileIssueNodeNode.getColStart() + 1,
                fileIssueNodeNode.getRowEnd() + 1,
                fileIssueNodeNode.getColEnd() + 1,
                fileIssueNodeNode.getLineSnippet());
    }
    
    /**
     * Converts a list of impact paths to an ImpactGraph.
     * Each path is a list of ImpactPath objects, representing a path from root to leaf.
     * Node names are "name:version" (or just "name" if version is empty).
     */
//    public static ImpactGraph toImpactGraph(List<List<ImpactPath>> impactPaths) {
//        // Use a dummy root node
//        ImpactTreeNode root = new ImpactTreeNode("root");
//        int maxDepth = 0;
//        for (List<ImpactPath> path : impactPaths) {
//            ImpactTreeNode current = root;
//            int depth = 0;
//            for (ImpactPath ip : path) {
//                String nodeName = ip.getName() + (ip.getVersion() != null && !ip.getVersion().isEmpty() ? ":" + ip.getVersion() : "");
//                current.getChildren().add(new ImpactTreeNode(nodeName));
//                depth++;
//            }
//            if (depth > maxDepth) {
//                maxDepth = depth;
//            }
//        }
//        ImpactGraphNode rootGraphNode = toImpactGraphNode(root);
//        // +1 to include root
//        maxDepth += 1;
//        // Set pathsLimit to the IMPACT_PATHS_LIMIT if maximum depth found exceeding the limit
//        return new ImpactGraph(rootGraphNode, maxDepth > IMPACT_PATHS_LIMIT ? IMPACT_PATHS_LIMIT : -1 );
//    } TODO: improve implementation
    
    public static ImpactGraph toImpactGraph(List<List<ImpactPath>> impactPaths) {
        if (impactPaths == null || impactPaths.isEmpty()) {
            return new ImpactGraph(new ImpactGraphNode("", new ImpactGraphNode[0]), 0);
        }
        // Use the first element in each path as the root for that path
        Map<String, ImpactTreeNode> rootMap = new LinkedHashMap<>();
        int maxDepth = 0;
        for (List<ImpactPath> path : impactPaths) {
            if (path == null || path.isEmpty()) {
                continue;
            }

            String rootName = getNodeName(path.get(0));
            ImpactTreeNode root = rootMap.computeIfAbsent(rootName, ImpactTreeNode::new);
            ImpactTreeNode current = root;
            int depth = 1;
            for (int i = 1; i < path.size(); i++) {
                String nodeName = getNodeName(path.get(i));
                current = getOrAddChild(current, nodeName);
                depth++;
            }
            if (depth > maxDepth) {
                maxDepth = depth;
            }
        }
        // Always use the first root in the map as the ImpactGraph root
        ImpactGraphNode rootGraphNode = toImpactGraphNode(rootMap.values().iterator().next());
        return new ImpactGraph(rootGraphNode, maxDepth);
    }

    private static String getNodeName(ImpactPath ip) {
        return ip.getName() + (ip.getVersion() != null && !ip.getVersion().isEmpty() ? ":" + ip.getVersion() : "");
    }

    // Helper to find or add a child node by name
    private static ImpactTreeNode getOrAddChild(ImpactTreeNode parent, String nodeName) {
        for (ImpactTreeNode child : parent.getChildren()) {
            if (child.getName().equals(nodeName)) {
                return child;
            }
        }
        ImpactTreeNode newChild = new ImpactTreeNode(nodeName);
        parent.getChildren().add(newChild);
        return newChild;
    }

    // Convert ImpactTreeNode to ImpactGraphNode tree
    private static ImpactGraphNode toImpactGraphNode(ImpactTreeNode impactTreeNode) {
        ImpactGraphNode[] children = impactTreeNode.getChildren().stream().map(WebviewObjectConverter::toImpactGraphNode).toArray(ImpactGraphNode[]::new);
        return new ImpactGraphNode(impactTreeNode.getName(), children);
    }
}
