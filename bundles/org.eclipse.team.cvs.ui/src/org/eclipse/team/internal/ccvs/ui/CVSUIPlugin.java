package org.eclipse.team.internal.ccvs.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.team.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.ccvs.core.ICVSRemoteFile;
import org.eclipse.team.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.core.client.listeners.ICommandOutputListener;
import org.eclipse.team.internal.ccvs.core.resources.ICVSFolder;
import org.eclipse.team.internal.ccvs.ui.model.CVSAdapterFactory;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * UI Plugin for CVS provider-specific workbench functionality.
 */
public class CVSUIPlugin extends AbstractUIPlugin {
	/**
	 * The id of the CVS plug-in
	 */
	public static final String ID = "org.eclipse.team.cvs.ui";
	
	private Hashtable imageDescriptors = new Hashtable(20);

	public final static String ICON_PATH;
	static {
		if (Display.getCurrent().getIconDepth() > 4) {
			ICON_PATH = ICVSUIConstants.ICON_PATH_FULL;
		} else {
			ICON_PATH = ICVSUIConstants.ICON_PATH_BASIC;
		}
	}

	/**
	 * The singleton plug-in instance
	 */
	private static CVSUIPlugin plugin;
	
	/**
	 * The repository manager
	 */
	private RepositoryManager repositoryManager;
	
	/**
	 * CVSUIPlugin constructor
	 * 
	 * @param descriptor  the plugin descriptor
	 */
	public CVSUIPlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		plugin = this;
	}

	/**
	 * Creates an image and places it in the image registry.
	 */
	protected void createImageDescriptor(String id, URL baseURL) {
		URL url = null;
		try {
			url = new URL(baseURL, ICON_PATH + id);
		} catch (MalformedURLException e) {
		}
		ImageDescriptor desc = ImageDescriptor.createFromURL(url);
		imageDescriptors.put(id, desc);
	}
	
	/**
	 * Returns the active workbench page.
	 * 
	 * @return the active workbench page
	 */
	public static IWorkbenchPage getActivePage() {
		IWorkbenchWindow window = getPlugin().getWorkbench().getActiveWorkbenchWindow();
		if (window == null) return null;
		return window.getActivePage();
	}
	
	/**
	 * Returns the image descriptor for the given image ID.
	 * Returns null if there is no such image.
	 */
	public ImageDescriptor getImageDescriptor(String id) {
		return (ImageDescriptor)imageDescriptors.get(id);
	}
	
	/**
	 * Returns the singleton plug-in instance.
	 * 
	 * @return the plugin instance
	 */
	public static CVSUIPlugin getPlugin() {
		return plugin;
	}

	/**
	 * Returns the repository manager
	 * 
	 * @return the repository manager
	 */
	public RepositoryManager getRepositoryManager() {
		return repositoryManager;
	}
	
	/**
	 * Initializes the table of images used in this plugin.
	 */
	private void initializeImages() {
		URL baseURL = getDescriptor().getInstallURL();
	
		// objects
		createImageDescriptor(ICVSUIConstants.IMG_REPOSITORY, baseURL); 
		createImageDescriptor(ICVSUIConstants.IMG_REFRESH, baseURL);
		createImageDescriptor(ICVSUIConstants.IMG_NEWLOCATION, baseURL);
		createImageDescriptor(ICVSUIConstants.IMG_TAG, baseURL);
		createImageDescriptor(ICVSUIConstants.IMG_CLEAR, baseURL);
		createImageDescriptor(ICVSUIConstants.IMG_BRANCHES_CATEGORY, baseURL);
		createImageDescriptor(ICVSUIConstants.IMG_VERSIONS_CATEGORY, baseURL);
		createImageDescriptor(ICVSUIConstants.IMG_PROJECT_VERSION, baseURL);
		createImageDescriptor(ICVSUIConstants.IMG_WIZBAN_BRANCH, baseURL);
		createImageDescriptor(ICVSUIConstants.IMG_WIZBAN_MERGE, baseURL);
		createImageDescriptor(ICVSUIConstants.IMG_WIZBAN_SHARE, baseURL);
	}
	/**
	 * Convenience method for logging statuses to the plugin log
	 * 
	 * @param status  the status to log
	 */
	public static void log(IStatus status) {
		getPlugin().getLog().log(status);
	}
	
	/**
	 * Initializes the preferences for this plugin if necessary.
	 */
	protected void initializePreferences() {
		IPreferenceStore store = getPreferenceStore();
		store.setDefault(ICVSUIConstants.PREF_SHOW_COMMENTS, true);
		store.setDefault(ICVSUIConstants.PREF_SHOW_TAGS, true);
		store.setDefault(ICVSUIConstants.PREF_PRUNE_EMPTY_DIRECTORIES, CVSProviderPlugin.DEFAULT_PRUNE);
		store.setDefault(ICVSUIConstants.PREF_TIMEOUT, CVSProviderPlugin.DEFAULT_TIMEOUT);
		store.setDefault(ICVSUIConstants.PREF_SHOW_MODULES, false);
		store.setDefault(ICVSUIConstants.PREF_HISTORY_TRACKS_SELECTION, false);
		
		store.setDefault(ICVSUIConstants.PREF_FILETEXT_DECORATION, CVSDecoratorConfiguration.DEFAULT_FILETEXTFORMAT);
		store.setDefault(ICVSUIConstants.PREF_FOLDERTEXT_DECORATION, CVSDecoratorConfiguration.DEFAULT_FOLDERTEXTFORMAT);
		store.setDefault(ICVSUIConstants.PREF_PROJECTTEXT_DECORATION, CVSDecoratorConfiguration.DEFAULT_PROJECTTEXTFORMAT);
		
		store.setDefault(ICVSUIConstants.PREF_ADDED_FLAG, CVSDecoratorConfiguration.DEFAULT_ADDED_FLAG);
		store.setDefault(ICVSUIConstants.PREF_DIRTY_FLAG, CVSDecoratorConfiguration.DEFAULT_DIRTY_FLAG);
				
		store.setDefault(ICVSUIConstants.PREF_SHOW_ADDED_DECORATION, true);
		store.setDefault(ICVSUIConstants.PREF_SHOW_HASREMOTE_DECORATION, true);
		store.setDefault(ICVSUIConstants.PREF_SHOW_DIRTY_DECORATION, true);
		store.setDefault(ICVSUIConstants.PREF_ADDED_FLAG, CVSDecoratorConfiguration.DEFAULT_ADDED_FLAG);
		store.setDefault(ICVSUIConstants.PREF_DIRTY_FLAG, CVSDecoratorConfiguration.DEFAULT_DIRTY_FLAG);
		store.setDefault(ICVSUIConstants.PREF_CALCULATE_DIRTY, true);
		
		// Forward the values to the CVS plugin
		CVSProviderPlugin.getPlugin().setPruneEmptyDirectories(store.getBoolean(ICVSUIConstants.PREF_PRUNE_EMPTY_DIRECTORIES));
		CVSProviderPlugin.getPlugin().setTimeout(store.getInt(ICVSUIConstants.PREF_TIMEOUT));
		CVSProviderPlugin.getPlugin().setQuietness(CVSPreferencesPage.getQuietnessOptionFor(store.getInt(ICVSUIConstants.PREF_QUIETNESS)));
	}

	/**
	 * @see Plugin#startup()
	 */
	public void startup() throws CoreException {
		super.startup();
		Policy.localize("org.eclipse.team.internal.ccvs.ui.messages");
		
		// XXX This should be refactored!!!
		Command.setConsoleListener(new ICommandOutputListener() {
			public IStatus messageLine(String line, ICVSFolder commandRoot, IProgressMonitor monitor) {
				Console.appendAll(line + '\n');
				return OK;
			}
			public IStatus errorLine(String line, ICVSFolder commandRoot, IProgressMonitor monitor) {
				Console.appendAll(line + '\n');
				return OK;
			}
		});

		CVSAdapterFactory factory = new CVSAdapterFactory();
		Platform.getAdapterManager().registerAdapters(factory, ICVSRemoteFile.class);
		Platform.getAdapterManager().registerAdapters(factory, ICVSRemoteFolder.class);
		Platform.getAdapterManager().registerAdapters(factory, ICVSRepositoryLocation.class);
		
		initializeImages();
		initializePreferences();
		repositoryManager = new RepositoryManager();
		try {
			repositoryManager.startup();
		} catch (TeamException e) {
			throw new CoreException(e.getStatus());
		}
	}
	
	/**
	 * @see Plugin#shutdown()
	 */
	public void shutdown() throws CoreException {
		super.shutdown();
		try {
			repositoryManager.shutdown();
		} catch (TeamException e) {
			throw new CoreException(e.getStatus());
		}
	}
}
