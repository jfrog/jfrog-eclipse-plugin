package org.jfrog.eclipse.scheduling;

import org.eclipse.core.runtime.jobs.ISchedulingRule;

/**
 * @author yahavi
 */
public class SchedulingRule implements ISchedulingRule {

	@Override
	public boolean contains(ISchedulingRule rule) {
		return rule == this;
	}

	@Override
	public boolean isConflicting(ISchedulingRule rule) {
		return rule == this;
	}
}
