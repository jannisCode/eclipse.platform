package org.eclipse.team.internal.ui;/* * (c) Copyright IBM Corp. 2000, 2002. * All Rights Reserved. */ import org.eclipse.jface.dialogs.InputDialog;import org.eclipse.jface.dialogs.MessageDialog;import org.eclipse.jface.preference.PreferencePage;import org.eclipse.jface.util.PropertyChangeEvent;import org.eclipse.swt.SWT;import org.eclipse.swt.layout.GridData;import org.eclipse.swt.layout.GridLayout;import org.eclipse.swt.widgets.Button;import org.eclipse.swt.widgets.Composite;import org.eclipse.swt.widgets.Control;import org.eclipse.swt.widgets.Event;import org.eclipse.swt.widgets.Label;import org.eclipse.swt.widgets.Listener;import org.eclipse.swt.widgets.Table;import org.eclipse.swt.widgets.TableItem;import org.eclipse.team.core.IIgnoreInfo;import org.eclipse.team.core.TeamPlugin;import org.eclipse.team.ui.TeamUIPlugin;import org.eclipse.ui.IWorkbench;import org.eclipse.ui.IWorkbenchPreferencePage;
public class IgnorePreferencePage extends PreferencePage implements IWorkbenchPreferencePage {	private Table ignoreTable;	private Button addButton;	private Button removeButton;
	public void init(IWorkbench workbench) {		setDescription(Policy.bind("IgnorePreferencePage.description"));	}		/**	 * Creates preference page controls on demand.	 *	 * @param parent  the parent for the preference page	 */	protected Control createContents(Composite ancestor) {		noDefaultAndApplyButton();				Composite parent = new Composite(ancestor, SWT.NULL);		GridLayout layout = new GridLayout();		layout.numColumns = 2;		parent.setLayout(layout);			// set F1 help		//WorkbenchHelp.setHelp(parent, new DialogPageContextComputer (this, IVCMHelpContextIds.IGNORE_PREFERENCE_PAGE));				Label l1 = new Label(parent, SWT.NULL);		l1.setText(Policy.bind("IgnorePreferencePage.ignorePatterns"));		GridData data = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);		data.horizontalSpan = 2;		l1.setLayoutData(data);				ignoreTable = new Table(parent, SWT.CHECK | SWT.BORDER);		GridData gd = new GridData(GridData.FILL_BOTH);		gd.widthHint= convertWidthInCharsToPixels(30);		ignoreTable.setLayoutData(gd);		ignoreTable.addListener(SWT.Selection, new Listener() {			public void handleEvent(Event e) {				handleSelection();			}		});				Composite buttons = new Composite(parent, SWT.NULL);		buttons.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));		buttons.setLayout(new GridLayout());				addButton = new Button(buttons, SWT.PUSH);		addButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));		addButton.setText(Policy.bind("IgnorePreferencePage.add"));		addButton.addListener(SWT.Selection, new Listener() {			public void handleEvent(Event e) {				addIgnore();			}		});						removeButton = new Button(buttons, SWT.PUSH);		removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));		removeButton.setText(Policy.bind("IgnorePreferencePage.remove"));		removeButton.setEnabled(false);		removeButton.addListener(SWT.Selection, new Listener() {			public void handleEvent(Event e) {				removeIgnore();			}		});				fillTable();				return parent;	}	/**	 * Do anything necessary because the OK button has been pressed.	 *	 * @return whether it is okay to close the preference page	 */	public boolean performOk() {		int count = ignoreTable.getItemCount();		String[] patterns = new String[count];		boolean[] enabled = new boolean[count];		TableItem[] items = ignoreTable.getItems();		for (int i = 0; i < count; i++) {			patterns[i] = items[i].getText();			enabled[i] = items[i].getChecked();		}		TeamPlugin.getManager().setGlobalIgnore(patterns, enabled);		TeamUIPlugin.getPlugin().broadcastPropertyChange(new PropertyChangeEvent(TeamUIPlugin.getPlugin(), TeamUIPlugin.GLOBAL_IGNORES_CHANGED, null, null));		return true;	}		private void fillTable() {		IIgnoreInfo[] ignore = TeamPlugin.getManager().getGlobalIgnore();		for (int i = 0; i < ignore.length; i++) {			IIgnoreInfo info = ignore[i];			TableItem item = new TableItem(ignoreTable, SWT.NONE);			item.setText(info.getPattern());			item.setChecked(info.getEnabled());		}	}		private void addIgnore() {		InputDialog dialog = new InputDialog(getShell(), Policy.bind("IgnorePreferencePage.enterPatternShort"), Policy.bind("IgnorePreferencePage.enterPatternLong"), null, null);		dialog.open();		if (dialog.getReturnCode() != InputDialog.OK) return;		String pattern = dialog.getValue();		if (pattern.equals("")) return;		// Check if the item already exists		TableItem[] items = ignoreTable.getItems();		for (int i = 0; i < items.length; i++) {			if (items[i].getText().equals(pattern)) {				MessageDialog.openWarning(getShell(), Policy.bind("IgnorePreferencePage.patternExistsShort"), Policy.bind("IgnorePreferencePage.patternExistsLong"));				return;			}		}		TableItem item = new TableItem(ignoreTable, SWT.NONE);		item.setText(pattern);		item.setChecked(true);	}		private void removeIgnore() {		int[] selection = ignoreTable.getSelectionIndices();		ignoreTable.remove(selection);	}	private void handleSelection() {		if (ignoreTable.getSelectionCount() > 0) {			removeButton.setEnabled(true);		} else {			removeButton.setEnabled(false);		}	}
}
