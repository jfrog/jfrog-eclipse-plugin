package com.jfrog.ide.eclipse.ui;

import com.jfrog.ide.common.filter.FilterManager;

/**
 * @author yahavi
 */
public class FilterManagerSingleton extends FilterManager {
	private static FilterManager instance = new FilterManagerSingleton();

	public static FilterManager getInstance() {
		return instance;
	}
}
