package org.jfrog.eclipse.ui.actions;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.ToolBar;
import org.jfrog.eclipse.ui.issues.IssuesFilterDialog;
import org.jfrog.eclipse.ui.licenses.LicensesFilterDialog;

/**
 * Open the filter dialog.
 * 
 * @author yahavi
 */
public class Filter extends Action {

	public enum FilterType {
		Severity, License
	}

	private FilterType filterType = FilterType.Severity;

	public Filter(ToolBar toolBar) {
		super(toolBar, "Filter", "filter");
		setFilterType(FilterType.Severity);
	}

	@Override
	public void execute(SelectionEvent event) {
		if (filterType == FilterType.Severity) {
			new IssuesFilterDialog(getDisplay().getActiveShell(), "Severities").open();
		} else {
			new LicensesFilterDialog(getDisplay().getActiveShell(), "Licenses").open();
		}
	}

	public void setFilterType(FilterType filterType) {
		this.filterType = filterType;
		setText(filterType.name());
	}
}
