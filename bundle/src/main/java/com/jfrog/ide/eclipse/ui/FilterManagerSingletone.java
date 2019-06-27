package com.jfrog.ide.eclipse.ui;

import com.jfrog.ide.common.filter.FilterManager;

/**
 * @author yahavi
 */
public class FilterManagerSingletone extends FilterManager {
	private static FilterManager instance = new FilterManagerSingletone();

	public static FilterManager getInstance() {
		return instance;
	}
}
