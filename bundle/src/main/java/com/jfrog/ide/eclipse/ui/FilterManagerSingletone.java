package com.jfrog.ide.eclipse.ui;

import com.jfrog.ide.common.filter.FilterManager;

public class FilterManagerSingletone extends FilterManager {
	private static FilterManager instance = new FilterManagerSingletone();

	public static FilterManager getInstance() {
		return instance;
	}
}
