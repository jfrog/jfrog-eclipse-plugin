package com.jfrog.ide.eclipse.ui;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.jfrog.build.extractor.scan.DependencyTree;

/**
 * Content provider for DependenciesTree.
 * 
 * @author yahavi
 */
public class ScanTreeContentProvider implements ITreeContentProvider {
	private static final DependencyTree[] EMPTY_NODE = new DependencyTree[0];

	@Override
	public Object[] getElements(Object element) {
		return getChildren(element);
	}

	@Override
	public Object[] getChildren(Object element) {
		return (((DependencyTree) element).getChildren()).toArray(EMPTY_NODE);
	}

	@Override
	public Object getParent(Object element) {
		return ((DependencyTree) element).getParent();
	}

	@Override
	public boolean hasChildren(Object element) {
		return !((DependencyTree) element).isLeaf();
	}
}
