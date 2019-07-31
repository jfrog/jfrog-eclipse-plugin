package com.jfrog.ide.eclipse.ui;

import com.jfrog.ide.common.filter.FilterManager;

/**
 * This class extends the FilterManager class, and adds to it the getInstance
 * method. The reason why the getInstance method is not part of the base class,
 * is because the base class is also used by the IDEA Plugin, for which the
 * getInstance method should not be exposed.
 * 
 * @author yahavi
 */
public class FilterManagerSingleton extends FilterManager {
	private static FilterManager instance = new FilterManagerSingleton();

	public static FilterManager getInstance() {
		return instance;
	}
}
