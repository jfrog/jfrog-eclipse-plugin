package com.jfrog.ide.eclipse.log;

import org.eclipse.core.runtime.IProgressMonitor;

import com.jfrog.ide.common.log.ProgressIndicator;

/**
 * @author yahavi
 */
public class ProgressIndicatorImpl implements ProgressIndicator {
	private static final int TOTAL_WORK = 100;

	IProgressMonitor progressMonitor;

	public ProgressIndicatorImpl(String name, IProgressMonitor progressMonitor) {
		this.progressMonitor = progressMonitor;
		this.progressMonitor.beginTask(name, TOTAL_WORK);
	}

	@Override
	public void setFraction(double fraction) {
		progressMonitor.worked((int) (fraction * TOTAL_WORK));
	}
}
