package com.jfrog.ide.eclipse.ui.webview;

import com.jfrog.ide.common.nodes.FileIssueNode;
import com.jfrog.ide.common.nodes.ScaIssueNode;
import com.jfrog.ide.common.nodes.SastIssueNode;
import com.jfrog.ide.common.nodes.subentities.ImpactPath;
import com.jfrog.ide.common.nodes.subentities.Severity;
import com.jfrog.ide.common.nodes.subentities.SourceCodeScanType;
import com.jfrog.ide.common.parse.Applicability;
import com.jfrog.ide.common.webview.*;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

public class WebviewObjectConverterTest extends TestCase{

    private ScaIssueNode scaIssueNode;
    private FileIssueNode secretIssueNode;
    private SastIssueNode sastIssueNode;
    
    // setup common data
    String filePath = "/test/path/file.java";
    int rowStart = 10;
    int colStart = 5;
    int rowEnd = 20;
    int colEnd = 10;
    String lineSnippet = "vulnerable code line";

    public void testConvertScaIssueToDepPage() {
    	// Setup SCA test data
    	String scaTitle = "CVE-2023-1234";
    	String reason = "sca issue reason";
    	Severity severity = Severity.High;
    	String ruleID = "CVE-2023-1234_test-component_1.0.0";
    	Applicability applicability = Applicability.APPLICABLE;
    	List<List<ImpactPath>> impactPaths = createTestImpactPaths();
    	String[] fixedVersions = {"[1.0.1]", "[1.0.2]"};
    	String fullDescription = "Test vulnerability description";
    	
        scaIssueNode = new ScaIssueNode(scaTitle, reason, severity, ruleID, applicability, impactPaths, fixedVersions, fullDescription);
        DependencyPage result = WebviewObjectConverter.convertScaIssueToDepPage(scaIssueNode);

        assertNotNull(result);
        assertEquals(scaIssueNode.getComponentName(), result.getComponent());
        assertEquals(scaIssueNode.getComponentVersion(), result.getVersion());
        assertEquals(scaIssueNode.getSeverity().getSeverityName(), result.getSeverity());
        assertEquals(scaIssueNode.getFullDescription(), result.getSummary());
        assertEquals(scaIssueNode.getFixedVersions(), result.getFixedVersion());
        assertNotNull(result.getCve());
        assertEquals(scaIssueNode.getTitle(), result.getCve().getId());
    }

    public void testConvertFileIssueToIssuePage() {
        // setup secrets test data
        String secretTitle = "Secret issue";
        String secretReason = "Hard coded secrets were found";
        Severity secretSeverity = Severity.Medium; 
        String secretRuleId = "SECRET-RULE"; 
        String secretFullDescription = "Test Secret issue description";
        
        secretIssueNode = new FileIssueNode(secretTitle, filePath, rowStart, colStart, rowEnd, colEnd, secretReason, lineSnippet, SourceCodeScanType.SECRETS, secretSeverity, secretRuleId, secretFullDescription);
        IssuePage result = WebviewObjectConverter.convertFileIssueToIssuePage(secretIssueNode);

        assertNotNull(result);
        assertEquals(secretIssueNode.getTitle(), result.getHeader());
        assertEquals(secretIssueNode.getSeverity().name(), result.getSeverity());
        assertEquals(secretIssueNode.getFullDescription(), result.getDescription());
        assertNotNull(result.getLocation());
        assertEquals(secretIssueNode.getFilePath(), result.getLocation().getFile());
        assertEquals(secretIssueNode.getRowStart() + 1, result.getLocation().getStartRow());
        assertEquals(secretIssueNode.getColStart() + 1, result.getLocation().getStartColumn());
    }

    public void testConvertSastIssueToSastIssuePage() {
        // setup SAST test data
        String sastTitle = "SAST Issue";
        String sastReason = "SAST issue reason";
        Severity sastSeverity = Severity.Critical;
        String sastRuleId = "SAST-RULE";
        String sastFullDescription = "Test SAST issue description";
        
        sastIssueNode = new SastIssueNode(sastTitle, filePath, rowStart, colStart, rowEnd, colEnd, sastReason, lineSnippet, null, sastSeverity, sastRuleId, sastFullDescription);
        
        SastIssuePage result = WebviewObjectConverter.convertSastIssueToSastIssuePage(sastIssueNode);

        assertNotNull(result);
        assertEquals(sastIssueNode.getTitle(), result.getHeader());
        assertEquals(sastIssueNode.getRuleId(), result.getRuleId());
        assertEquals(sastIssueNode.getSeverity().name(), result.getSeverity());
        assertEquals(sastIssueNode.getFullDescription(), result.getDescription());
    }

    public void testToImpactGraph_EmptyInput() {
        ImpactGraph result = WebviewObjectConverter.toImpactGraph(null);

        assertNotNull(result);
        assertNotNull(result.getRoot());
        assertEquals("", result.getRoot().getName());
        assertEquals(0, result.getRoot().getChildren().length);
    }

    public void testToImpactGraph_SinglePath() {
        List<List<ImpactPath>> impactPaths = new ArrayList<>();
        List<ImpactPath> path = new ArrayList<>();
        path.add(new ImpactPath("root", "1.0"));
        path.add(new ImpactPath("child1", "2.0"));
        path.add(new ImpactPath("child2", "3.0"));
        impactPaths.add(path);

        ImpactGraph result = WebviewObjectConverter.toImpactGraph(impactPaths);
        
        assertNotNull(result);
        assertNotNull(result.getRoot());
        assertEquals("root:1.0", result.getRoot().getName());
        assertEquals(1, result.getRoot().getChildren().length);
        assertEquals("child1:2.0", result.getRoot().getChildren()[0].getName());
        assertEquals(1, result.getRoot().getChildren()[0].getChildren().length);
        assertEquals("child2:3.0", result.getRoot().getChildren()[0].getChildren()[0].getName());
        // validate IMPACT_PATHS_LIMIT wasn't exceeded 
        assertEquals(-1, result.getPathsLimit());
    }

    public void testToImpactGraph_MultiplePaths() {
        List<List<ImpactPath>> impactPaths = new ArrayList<>();
        
        // First path
        List<ImpactPath> path1 = new ArrayList<>();
        path1.add(new ImpactPath("root", "1.0"));
        path1.add(new ImpactPath("child1", "2.0"));
        impactPaths.add(path1);

        // Second path
        List<ImpactPath> path2 = new ArrayList<>();
        path2.add(new ImpactPath("root", "1.0"));
        path2.add(new ImpactPath("child2", "3.0"));
        impactPaths.add(path2);

        ImpactGraph result = WebviewObjectConverter.toImpactGraph(impactPaths);

        assertNotNull(result);
        assertNotNull(result.getRoot());
        assertEquals("root:1.0", result.getRoot().getName());
        assertEquals(2, result.getRoot().getChildren().length);
        
        // Verify both children exist
        boolean hasChild1 = false;
        boolean hasChild2 = false;
        for (ImpactGraphNode child : result.getRoot().getChildren()) {
            if (child.getName().equals("child1:2.0")) hasChild1 = true;
            if (child.getName().equals("child2:3.0")) hasChild2 = true;
        }
        assertTrue(hasChild1 && hasChild2);
    }

    public void testToImpactGraph_ExceedsLimit() {
        List<List<ImpactPath>> impactPaths = new ArrayList<>();
        for (int i = 0; i < WebviewObjectConverter.IMPACT_PATHS_LIMIT + 5; i++) {
            List<ImpactPath> path = new ArrayList<>();
            path.add(new ImpactPath("root" + i, "1.0"));
            path.add(new ImpactPath("child" + i, "2.0"));
            impactPaths.add(path);
        }

        ImpactGraph result = WebviewObjectConverter.toImpactGraph(impactPaths);

        assertNotNull(result);
        assertEquals(WebviewObjectConverter.IMPACT_PATHS_LIMIT, result.getPathsLimit());
    }

    private List<List<ImpactPath>> createTestImpactPaths() {
        List<List<ImpactPath>> impactPaths = new ArrayList<>();
        List<ImpactPath> path = new ArrayList<>();
        path.add(new ImpactPath("root", "1.0"));
        path.add(new ImpactPath("child1", "2.0"));
        path.add(new ImpactPath("child2", "3.0"));
        impactPaths.add(path);
        return impactPaths;
    }
} 