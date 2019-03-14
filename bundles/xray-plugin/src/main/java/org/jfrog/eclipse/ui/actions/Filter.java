package org.jfrog.eclipse.ui.actions;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.ISharedImages;
import org.jfrog.eclipse.ui.issues.IssuesFilterDialog;
import org.jfrog.eclipse.ui.licenses.LicensesFilterDialog;

/**
 * @author yahavi
 */
public class Filter extends Action {

	public enum FilterType {
		Severity, License
	}

	private FilterType filterType = FilterType.Severity;

	public Filter(ToolBar toolBar) {
		super(toolBar, ISharedImages.IMG_ETOOL_CLEAR);
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
