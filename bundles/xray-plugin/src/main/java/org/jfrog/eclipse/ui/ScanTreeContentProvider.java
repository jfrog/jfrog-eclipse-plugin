package org.jfrog.eclipse.ui;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.jfrog.build.extractor.scan.DependenciesTree;

/**
 * Content provider for DependenciesTree.
 * 
 * @author yahavi
 */
public class ScanTreeContentProvider implements ITreeContentProvider {
	private static final DependenciesTree[] EMPTY_NODE = new DependenciesTree[0];

	@Override
	public Object[] getElements(Object element) {
		return getChildren(element);
	}

	@Override
	public Object[] getChildren(Object element) {
		return (((DependenciesTree) element).getChildren()).toArray(EMPTY_NODE);
	}

	@Override
	public Object getParent(Object element) {
		return ((DependenciesTree) element).getParent();
	}

	@Override
	public boolean hasChildren(Object element) {
		return !((DependenciesTree) element).isLeaf();
	}
}