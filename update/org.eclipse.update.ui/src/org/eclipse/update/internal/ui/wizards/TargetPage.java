package org.eclipse.update.internal.ui.wizards;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import java.util.*;
import org.eclipse.swt.SWT;
import org.eclipse.update.internal.ui.model.*;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.jface.viewers.*;
import org.eclipse.update.internal.ui.parts.*;
import org.eclipse.update.core.*;
import org.eclipse.update.configuration.*;
import org.eclipse.update.internal.ui.*;
import java.net.URL;
import java.io.*;
import org.eclipse.core.boot.IPlatformConfiguration;
import org.eclipse.jface.dialogs.MessageDialog;

public class TargetPage extends BannerPage {
	// NL keys
	private static final String KEY_TITLE = "InstallWizard.TargetPage.title";
	private static final String KEY_DESC = "InstallWizard.TargetPage.desc";
	private static final String KEY_NEW = "InstallWizard.TargetPage.new";
	private static final String KEY_REQUIRED_FREE_SPACE =
		"InstallWizard.TargetPage.requiredSpace";
	private static final String KEY_AVAILABLE_FREE_SPACE =
		"InstallWizard.TargetPage.availableSpace";
	private static final String KEY_LOCATION = "InstallWizard.TargetPage.location";
	private static final String KEY_LOCATION_MESSAGE =
		"InstallWizard.TargetPage.location.message";
	private static final String KEY_LOCATION_EMPTY =
		"InstallWizard.TargetPage.location.empty";
	private static final String KEY_LOCATION_ERROR_TITLE =
		"InstallWizard.TargetPage.location.error.title";
	private static final String KEY_LOCATION_ERROR_MESSAGE =
		"InstallWizard.TargetPage.location.error.message";
	private static final String KEY_SIZE =
		"InstallWizard.TargetPage.size";
	private static final String KEY_SIZE_UNKNOWN = 
		"InstallWizard.TargetPage.unknownSize";
	private TableViewer tableViewer;
	private IInstallConfiguration config;
	private Image siteImage;
	private ConfigListener configListener;
	private Label requiredSpaceLabel;
	private Label availableSpaceLabel;
	private IFeature feature;

	class TableContentProvider
		extends DefaultContentProvider
		implements IStructuredContentProvider {

		/**
		 * @see IStructuredContentProvider#getElements(Object)
		 */
		public Object[] getElements(Object parent) {
			return config.getConfiguredSites();
		}
	}

	class TableLabelProvider extends LabelProvider implements ITableLabelProvider {
		/**
		* @see ITableLabelProvider#getColumnImage(Object, int)
		*/
		public Image getColumnImage(Object obj, int col) {
			return siteImage;
		}

		/**
		 * @see ITableLabelProvider#getColumnText(Object, int)
		 */
		public String getColumnText(Object obj, int col) {
			if (obj instanceof IConfiguredSite && col == 0) {
				IConfiguredSite csite = (IConfiguredSite) obj;
				ISite site = csite.getSite();
				URL url = site.getURL();
				return url.toString();
			}
			return null;
		}

	}

	class ConfigListener implements IInstallConfigurationChangedListener {
		public void installSiteAdded(IConfiguredSite csite) {
			tableViewer.add(csite);
			tableViewer.setSelection(new StructuredSelection(csite));
		}

		public void installSiteRemoved(IConfiguredSite csite) {
			tableViewer.remove(csite);
		}
	}

	/**
	 * Constructor for ReviewPage
	 */
	public TargetPage(IFeature feature, IInstallConfiguration config) {
		super("Target");
		setTitle(UpdateUIPlugin.getResourceString(KEY_TITLE));
		setDescription(UpdateUIPlugin.getResourceString(KEY_DESC));
		this.config = config;
		this.feature = feature;
		siteImage = UpdateUIPluginImages.DESC_SITE_OBJ.createImage();
		configListener = new ConfigListener();
	}

	public void dispose() {
		if (siteImage != null) {
			siteImage.dispose();
			siteImage = null;
		}
		config.removeInstallConfigurationChangedListener(configListener);
		super.dispose();
	}

	/**
	 * @see DialogPage#createControl(Composite)
	 */
	public Control createContents(Composite parent) {
		Composite client = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = layout.marginHeight = 0;
		client.setLayout(layout);
		createTableViewer(client);
		Composite buttonContainer = new Composite(client, SWT.NULL);
		GridLayout blayout = new GridLayout();
		blayout.marginWidth = blayout.marginHeight = 0;
		buttonContainer.setLayout(blayout);
		GridData gd = new GridData(GridData.FILL_VERTICAL);
		buttonContainer.setLayoutData(gd);
		final Button button = new Button(buttonContainer, SWT.PUSH);
		button.setText(UpdateUIPlugin.getResourceString(KEY_NEW));
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				addTargetLocation();
			}
		});
		gd = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
		button.setLayoutData(gd);
		SWTUtil.setButtonDimensionHint(button);
		Composite status = new Composite(client, SWT.NULL);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan = 2;
		status.setLayoutData(gd);
		layout = new GridLayout();
		layout.numColumns = 2;
		status.setLayout(layout);
		Label label = new Label(status, SWT.NULL);
		label.setText(UpdateUIPlugin.getResourceString(KEY_REQUIRED_FREE_SPACE));
		requiredSpaceLabel = new Label(status, SWT.NULL);
		requiredSpaceLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		label = new Label(status, SWT.NULL);
		label.setText(UpdateUIPlugin.getResourceString(KEY_AVAILABLE_FREE_SPACE));
		availableSpaceLabel = new Label(status, SWT.NULL);
		availableSpaceLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		tableViewer.setInput(UpdateUIPlugin.getDefault().getUpdateModel());
		selectFirstTarget();
		return client;
	}
	private void createTableViewer(Composite parent) {
		tableViewer = new TableViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_BOTH);
		Table table = tableViewer.getTable();
		table.setLayoutData(gd);
		tableViewer.setContentProvider(new TableContentProvider());
		tableViewer.setLabelProvider(new TableLabelProvider());
		tableViewer.addFilter(new ViewerFilter() {
			public boolean select(Viewer v, Object parent, Object obj) {
				IConfiguredSite site = (IConfiguredSite) obj;
				return site.verifyUpdatableStatus().isOK();
			}
		});
		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection selection = event.getSelection();
				boolean empty = selection.isEmpty();
				verifyNotEmpty(empty);
				updateStatus(((IStructuredSelection) selection).getFirstElement());
			}
		});
		
		if (config!=null)
		config.addInstallConfigurationChangedListener(configListener);
	}

	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			tableViewer.getTable().setFocus();
		}
	}

	private void verifyNotEmpty(boolean empty) {
		String errorMessage = null;
		if (empty)
			errorMessage = UpdateUIPlugin.getResourceString(KEY_LOCATION_EMPTY);
		setErrorMessage(errorMessage);
		setPageComplete(!empty);
	}

	private void selectFirstTarget() {
		IConfiguredSite[] sites = config.getConfiguredSites();
		IConfiguredSite firstSite = null;
		for (int i = 0; i < sites.length; i++) {
			IConfiguredSite csite = sites[i];
			if (csite.verifyUpdatableStatus().isOK()) {
				firstSite = csite;
				break;
			}

		}
		if (firstSite != null) {
			tableViewer.setSelection(new StructuredSelection(firstSite));
		}
	}

	private void addTargetLocation() {
		DirectoryDialog dd = new DirectoryDialog(getContainer().getShell());
		dd.setMessage(UpdateUIPlugin.getResourceString(KEY_LOCATION_MESSAGE));
		String path = dd.open();
		if (path != null) {
			try {
				File file = new File(path);
				IConfiguredSite csite = config.createConfiguredSite(file);
				if (csite.verifyUpdatableStatus().isOK())
					config.addConfiguredSite(csite);
				else {
					String title = UpdateUIPlugin.getResourceString(KEY_LOCATION_ERROR_TITLE);
					String message =
						UpdateUIPlugin.getFormattedMessage(KEY_LOCATION_ERROR_MESSAGE, path);
					MessageDialog.openError(getContainer().getShell(), title, message);
				}
			} catch (CoreException e) {
				UpdateUIPlugin.logException(e);
			}
		}
	}

	private void updateStatus(Object element) {
		if (element == null) {
			requiredSpaceLabel.setText("");
			availableSpaceLabel.setText("");
			return;
		}
		IConfiguredSite site = (IConfiguredSite) element;
		URL url = site.getSite().getURL();
		String fileName = url.getFile();
		File file = new File(fileName);
		long available = LocalSystemInfo.getFreeSpace(file);
		long required = site.getSite().getInstallSizeFor(feature);
		if (required == -1)
			requiredSpaceLabel.setText(UpdateUIPlugin.getResourceString(KEY_SIZE_UNKNOWN));
		else
			requiredSpaceLabel.setText(UpdateUIPlugin.getFormattedMessage(KEY_SIZE, ""+required));

		if (available == LocalSystemInfo.SIZE_UNKNOWN)
			availableSpaceLabel.setText(UpdateUIPlugin.getResourceString(KEY_SIZE_UNKNOWN));
		else
			availableSpaceLabel.setText(UpdateUIPlugin.getFormattedMessage(KEY_SIZE, ""+available));
	}

	public IConfiguredSite getTargetSite() {
		IStructuredSelection sel = (IStructuredSelection) tableViewer.getSelection();
		if (sel.isEmpty())
			return null;
		return (IConfiguredSite) sel.getFirstElement();
	}
}