package org.eclipse.team.internal.ccvs.core.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.io.File;

import org.eclipse.team.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.core.IIgnoreInfo;
import org.eclipse.team.core.TeamPlugin;
import org.eclipse.team.core.internal.IgnoreInfo;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSyncInfo;
import org.eclipse.team.internal.ccvs.core.util.FileNameMatcher;
import org.eclipse.team.internal.ccvs.core.util.FileUtil;
import org.eclipse.team.internal.ccvs.core.util.SyncFileUtil;
import org.eclipse.team.internal.ccvs.core.util.Util;

/**
 * Represents handles to CVS resource on the local file system. Synchronization
 * information is taken from the CVS subdirectories. 
 * 
 * @see LocalFolder
 * @see LocalFile
 */
public abstract class LocalResource implements ICVSResource {

	 // The seperator that must be used when creating CVS resource paths. Never use
	 // the platform default seperator since it is not compatible with CVS resources.
	protected static final String SEPARATOR = "/";
		
	/**
	 * The local file represented by this handle.
	 */
	File ioResource;
	
	/**
	 * A local handle 
	 */
	public LocalResource(File ioResource) {
		this.ioResource = ioResource;
	}
	
	/**
	 * Get the extention of the path of resource
	 * relative to the path of root
	 * 
	 * @throws CVSException if root is not a root-folder of resource
	 */
	public String getRelativePath(ICVSFolder root) 
		throws CVSException {
		
		LocalResource rootFolder;
		String result;
		
		try {
			rootFolder = (LocalResource)root;
		} catch (ClassCastException e) {
			throw new CVSException(0,0,"two different implementations of ICVSResource used",e);
		}
		
		result = Util.getRelativePath(rootFolder.getPath(),getPath()); 
		return result;	
	}

	/**
	 * Do a DEEP delete.
	 * @see ICVSResource#delete()
	 */
	public void delete() {
		FileUtil.deepDelete(ioResource);
		// XXX Should we clear the cache in all cases?
		// XXX If not, should we provide a boolean parameter as a choice
	}

	/**
	 * @see ICVSResource#exists()
	 */
	public boolean exists() {
		return ioResource.exists();
	}

	/**
	 * @see ICVSResource#getParent()
	 */
	public ICVSFolder getParent() {
		return new LocalFolder(ioResource.getParentFile());
	}

	/**
	 * @see ICVSResource#getName()
	 */
	public String getName() {
		return ioResource.getName();
	}

	/**
	 * @see ICVSResource#isIgnored()
	 */
	public boolean isIgnored() {
		// check both the global patterns, the default ignores, and cvs ignore files				
		IIgnoreInfo[] ignorePatterns = TeamPlugin.getManager().getGlobalIgnore();
		FileNameMatcher matcher = new FileNameMatcher(SyncFileUtil.PREDEFINED_IGNORE_PATTERNS);
		for (int i = 0; i < ignorePatterns.length; i++) {
			IIgnoreInfo info = ignorePatterns[i];
			if(info.getEnabled()) {
				matcher.register(info.getPattern(), "true");
			}
		}
		boolean ignored = matcher.match(ioResource.getName());
		if(!ignored) {
			ignored = CVSProviderPlugin.getSynchronizer().isIgnored(ioResource);		
		}
		
		if(!ignored) {
			ICVSFolder parent = getParent();
			if(((LocalResource)parent).getLocalFile()==null) return false;
			return parent.isIgnored();
		} else {
			return ignored;
		}
	}

	public void setIgnored() throws CVSException {
		CVSProviderPlugin.getSynchronizer().setIgnored(ioResource, null);
	}
	
	public void setIgnoredAs(String pattern) throws CVSException {
		CVSProviderPlugin.getSynchronizer().setIgnored(ioResource, pattern);		
	}

	/**
	 * @see ICVSResource#isManaged()
	 */
	public boolean isManaged() {
		try {
			return getSyncInfo() != null;
		} catch(CVSException e) {
			return false;
		}
	}
			
	/**
	 * Two ManagedResources are equal, if there cvsResources are
	 * equal (and that is, if the point to the same file)
	 */
	public boolean equals(Object obj) {
		
		if (!(obj instanceof LocalResource)) {
			return false;
		} else {
			return getPath().equals(((LocalResource) obj).getPath());
		}
	}
			
	/*
	 * @see ICVSResource#getPath()
	 */
	public String getPath() {
		return ioResource.getAbsolutePath();
	}	
	
	/*
	 * @see ICVSResource#isFolder()
	 */
	public boolean isFolder() {
		return false;
	}
	
	/*
	 * @see ICVSResource#getSyncInfo()
	 */
	public ResourceSyncInfo getSyncInfo() throws CVSException {
		return CVSProviderPlugin.getSynchronizer().getResourceSync(ioResource);
	}

	/*
	 * @see ICVSResource#setSyncInfo(ResourceSyncInfo)
	 */
	public void setSyncInfo(ResourceSyncInfo info) throws CVSException {
		CVSProviderPlugin.getSynchronizer().setResourceSync(ioResource, info);		
	}
	
	/*
	 * Implement the hashcode on the underlying strings, like it is done in the equals.
	 */
	public int hashCode() {
		return getPath().hashCode();
	}	
	
	/*
	 * Give the pathname back
	 */
	public String toString() {
		return getPath();
	}
	
	public File getLocalFile() {
		return ioResource;
	}
}