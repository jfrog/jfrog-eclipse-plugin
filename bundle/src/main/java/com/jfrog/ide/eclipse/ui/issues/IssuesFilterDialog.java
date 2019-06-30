package com.jfrog.ide.eclipse.ui.issues;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.jfrog.build.extractor.scan.Severity;

import com.jfrog.ide.eclipse.ui.FilterDialog;
import com.jfrog.ide.eclipse.ui.FilterManagerSingleton;

/**
 * @author yahavi
 */
public class IssuesFilterDialog extends FilterDialog {

	private static MutableBoolean selectAllState = new MutableBoolean(true);

	public IssuesFilterDialog(Shell parentShell, String title) {
		super(parentShell, title, selectAllState);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		FilterManagerSingleton.getInstance().getSelectedSeverities().forEach((severity, isSelected) -> {
			buttons.add(new SeverityButton(container, severity, isSelected));
		});
		return container;
	}

	@Override
	protected void selectAll() {
		FilterManagerSingleton.getInstance().getSelectedSeverities().replaceAll((severity, isSelected) -> {
			return selectAllButton.getSelection();
		});
		IssuesTree.getInstance().applyFiltersForAllProjects();
	}

	private class SeverityButton extends FilterButton {
		public SeverityButton(Composite parent, Severity severity, boolean isSelected) {
			super(parent, severity.getSeverityName(), isSelected);
			addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent event) {
					super.widgetSelected(event);
					Button button = (Button) event.getSource();
					FilterManagerSingleton.getInstance().getSelectedSeverities().replace(severity,
							button.getSelection());
					IssuesTree.getInstance().applyFiltersForAllProjects();
				}
			});
		}
	}
}
