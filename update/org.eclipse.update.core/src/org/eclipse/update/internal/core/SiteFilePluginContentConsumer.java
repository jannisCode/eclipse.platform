package org.eclipse.update.internal.core;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import org.eclipse.core.runtime.*;
import org.eclipse.update.core.*;
import org.eclipse.update.core.model.PluginEntryModel;

/**
 * Plugin Content Consumer on a Site
 */
public class SiteFilePluginContentConsumer extends ContentConsumer {

	private IPluginEntry pluginEntry;
	private ISite site;
	
	// recovery
	private String oldPath;
	private String newPath;
	

	/*
	 * Constructor
	 */
	public SiteFilePluginContentConsumer(IPluginEntry pluginEntry, ISite site) {
		this.pluginEntry = pluginEntry;
		this.site = site;
	}

	/*
	 * @see ISiteContentConsumer#store(ContentReference, IProgressMonitor)
	 */
	public void store(ContentReference contentReference, IProgressMonitor monitor)
		throws CoreException {
		InputStream inStream = null;
		String pluginPath = null;

		try {
			URL newURL =
				new URL(
					site.getURL(),
					Site.DEFAULT_PLUGIN_PATH + pluginEntry.getVersionedIdentifier().toString());
			pluginPath = newURL.getFile();
			String contentKey = contentReference.getIdentifier();
			inStream = contentReference.getInputStream();
			pluginPath += pluginPath.endsWith(File.separator)
				? contentKey
				: File.separator + contentKey;

			// error recovery
			if (pluginPath.endsWith("plugin.xml") || pluginPath.endsWith("fragment.xml")) {
				oldPath=pluginPath.replace(File.separatorChar,'/');
				pluginPath = ErrorRecoveryLog.getLocalRandomIdentifier(pluginPath);
				newPath=pluginPath;
				ErrorRecoveryLog.getLog().appendPath(ErrorRecoveryLog.PLUGIN_ENTRY, pluginPath);
			}
			UpdateManagerUtils.copyToLocal(inStream, pluginPath, null);
		} catch (IOException e) {
			throw Utilities.newCoreException(
				Policy.bind("GlobalConsumer.ErrorCreatingFile", pluginPath),
				e);
			//$NON-NLS-1$
		} finally {
			try {
				// close stream
				inStream.close();
			} catch (Exception e) {
			}
		}
	}

	/*
	 * @see ISiteContentConsumer#close() 
	 */
	public void close() throws CoreException {
	
		if (newPath!=null){
			// rename file 
			ErrorRecoveryLog.getLog().appendPath(ErrorRecoveryLog.RENAME_ENTRY, newPath);		
			File fileToRename = new File(newPath);
			boolean sucess = false;
			if (fileToRename.exists()){
				File renamedFile = new File(oldPath);
				sucess = fileToRename.renameTo(renamedFile);
			}
			if(!sucess){
				String msg = Policy.bind("ContentConsumer.UnableToRename",newPath,oldPath);
				throw Utilities.newCoreException(msg,new Exception(msg));
			}
		}
		if (site instanceof SiteFile)
			 ((SiteFile) site).addPluginEntry(pluginEntry);
	}

	/*
	 * 
	 */
	public void abort() throws CoreException {
		// FIXME
		try {
			URL newURL =
				new URL(
					site.getURL(),
					Site.DEFAULT_PLUGIN_PATH + pluginEntry.getVersionedIdentifier().toString());
			String pluginPath = newURL.getFile();
			UpdateManagerUtils.removeFromFileSystem(new File(pluginPath));
		} catch (MalformedURLException e) {
			// FIXME
			throw Utilities.newCoreException(e.getMessage(), e);
		}
	}

}