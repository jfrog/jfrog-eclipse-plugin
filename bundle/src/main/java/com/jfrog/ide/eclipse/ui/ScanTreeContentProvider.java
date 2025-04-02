package com.jfrog.ide.eclipse.ui;

import java.util.List;

import javax.swing.tree.TreeNode;

import org.eclipse.jface.viewers.ITreeContentProvider;

import com.jfrog.ide.common.nodes.FileIssueNode;
import com.jfrog.ide.common.nodes.FileTreeNode;

/**
 * Content provider for DependenciesTree.
 * 
 * @author yahavi
 */
public class ScanTreeContentProvider implements ITreeContentProvider {

	@Override
	public Object[] getElements(Object element) {
		return getChildren(element);
	}

	@Override
	public Object[] getChildren(Object element) {
        if (element instanceof FileTreeNode) {
            List<TreeNode> children = ((FileTreeNode) element).getChildren();
            return children.toArray();
        } else if (element instanceof List) {
            // If the element is a List (root nodes), return its elements
            List<?> list = (List<?>) element;
            return list.toArray();
        }
        return new Object[0];
	}

	@Override
	public Object getParent(Object element) {
        if (element instanceof FileTreeNode) {
            return ((FileTreeNode) element).getParent();
        }
        if (element instanceof FileIssueNode) {
            return ((FileIssueNode) element).getParent();
        }
        return null;
	}

	@Override
	public boolean hasChildren(Object element) {
        if (element instanceof FileTreeNode) {
            return !((FileTreeNode) element).isLeaf();
        }
        if (element instanceof FileIssueNode) {
            return ((FileIssueNode) element).isLeaf();
        }
        return false;
	}
}
