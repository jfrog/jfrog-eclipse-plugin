package com.jfrog.ide.eclipse.scan;

import java.util.ArrayList;
import java.util.List;

import com.jfrog.ide.common.nodes.FileTreeNode;
import com.jfrog.ide.common.utils.XrayConnectionUtils.Results;

public class ScanCache {
	private List<FileTreeNode> scanResults;
	
	private static ScanCache instance;
	
	private ScanCache() {
		scanResults = new ArrayList<FileTreeNode>();
	}
	
    public static ScanCache getInstance() {
        if (instance == null) {
            synchronized (ScanCache.class) {
                if (instance == null) {
                    instance = new ScanCache();
                }
            }
        }
        return instance;
    }
    
    public List<FileTreeNode> getScanResults() {
    	return scanResults;
    }
    
    public void updateScanResults(List<FileTreeNode> results) {
    	if (results != null) {
    		scanResults.addAll(results);
    	}
    }
    
    public void resetCache() {
    	scanResults = new ArrayList<FileTreeNode>();
    }
}