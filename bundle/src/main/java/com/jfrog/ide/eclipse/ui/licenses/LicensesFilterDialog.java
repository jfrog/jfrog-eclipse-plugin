package com.jfrog.ide.eclipse.ui.licenses;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.jfrog.build.extractor.scan.License;

import com.jfrog.ide.eclipse.ui.FilterDialog;
import com.jfrog.ide.eclipse.ui.FilterManagerSingletone;

/**
 * @author yahavi
 */
public class LicensesFilterDialog extends FilterDialog {

	private static MutableBoolean selectAllState = new MutableBoolean(true);

	public LicensesFilterDialog(Shell parentShell, String title) {
		super(parentShell, title, selectAllState);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		FilterManagerSingletone.getInstance().getSelectedLicenses().forEach((license, isSelected) -> {
			buttons.add(new LicenseButton(container, license, isSelected));
		});
		return container;
	}

	private class LicenseButton extends FilterButton {
		public LicenseButton(Composite parent, License license, boolean isSelected) {
			super(parent, license.getName(), isSelected);
			addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent event) {
					super.widgetSelected(event);
					Button button = (Button) event.getSource();
					FilterManagerSingletone.getInstance().getSelectedLicenses().replace(license, button.getSelection());
					LicensesTree.getInstance().applyFiltersForAllProjects();
				}
			});
		}
	}

	@Override
	protected void selectAll() {
		FilterManagerSingletone.getInstance().getSelectedLicenses().replaceAll((license, isSelected) -> {
			return selectAllButton.getSelection();
		});
		LicensesTree.getInstance().applyFiltersForAllProjects();
	}

}
