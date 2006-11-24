/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.jarprocessor;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

/**
 * @author aniefer@ca.ibm.com
 *
 */
public class SignCommandStep extends CommandStep {
	private Set exclusions = null;

	public SignCommandStep(Properties options, String command) {
		super(options, command, ".jar", false); //$NON-NLS-1$
		exclusions = Utils.getSignExclusions(options);
	}

	public SignCommandStep(Properties options, String command, boolean verbose) {
		super(options, command, ".jar", verbose); //$NON-NLS-1$
		exclusions = Utils.getSignExclusions(options);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.update.jarprocessor.IProcessStep#recursionEffect(java.lang.String)
	 */
	public String recursionEffect(String entryName) {
		if (entryName.endsWith(extension) && !exclusions.contains(entryName))
			return entryName;
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.update.jarprocessor.IProcessStep#preProcess(java.io.File, java.io.File)
	 */
	public File preProcess(File input, File workingDirectory, List containers) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.update.jarprocessor.IProcessStep#postProcess(java.io.File, java.io.File)
	 */
	public File postProcess(File input, File workingDirectory, List containers) {
		if (command != null && shouldSign(input, containers)) {
			try {
				String[] cmd = new String[] {command, input.getCanonicalPath()};
				int result = execute(cmd, verbose);
				if (result == 0) {
					normalize(input, workingDirectory);
					return input;
				} else if (verbose) {
					System.out.println("Error: " + result + " was returned from command: " + Utils.concat(cmd)); //$NON-NLS-1$ //$NON-NLS-2$
				}
			} catch (IOException e) {
				if (verbose) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	private boolean shouldSign(File input, List containers) {
		Properties inf = null;

		//1: Are we excluded from signing by our parents?
		//innermost jar is first on the list, it overrides outer jars
		for (Iterator iterator = containers.iterator(); iterator.hasNext();) {
			inf = (Properties) iterator.next();
			if (inf.containsKey(Utils.MARK_EXCLUDE_CHILDREN_SIGN)){
				if(Boolean.valueOf(inf.getProperty(Utils.MARK_EXCLUDE_CHILDREN_SIGN)).booleanValue()) {
					if (verbose)
						System.out.println(input.getName() + "is excluded from signing by its containers."); //$NON-NLS-1$ //$NON-NLS-2$
					return false;
				}
				break;
			}
		}

		//2: Is this jar itself marked as exclude?
		inf = Utils.getEclipseInf(input, verbose);
		if (inf != null && inf.containsKey(Utils.MARK_EXCLUDE_SIGN) && Boolean.valueOf(inf.getProperty(Utils.MARK_EXCLUDE_SIGN)).booleanValue()) {
			if (verbose)
				System.out.println("Excluding " + input.getName() + " from signing."); //$NON-NLS-1$ //$NON-NLS-2$
			return false;
		}
		return true;
	}

	private void normalize(File input, File workingDirectory) {
		try {
			File tempJar = new File(workingDirectory, "temp_" + input.getName()); //$NON-NLS-1$
			JarFile jar = new JarFile(input, false);
			JarOutputStream jarOut = new JarOutputStream(new BufferedOutputStream(new FileOutputStream(tempJar)));
			InputStream in = null;
			try {
				Enumeration entries = jar.entries();
				for (JarEntry entry = (JarEntry) entries.nextElement(); entry != null; entry = entries.hasMoreElements() ? (JarEntry) entries.nextElement() : null) {
					JarEntry newEntry = new JarEntry(entry.getName());
					newEntry.setTime(entry.getTime());
					in = new BufferedInputStream(jar.getInputStream(entry));
					jarOut.putNextEntry(newEntry);
					Utils.transferStreams(in, jarOut, false);
					jarOut.closeEntry();
					in.close();
				}
			} finally {
				Utils.close(jarOut);
				Utils.close(jar);
				Utils.close(in);
			}
			tempJar.setLastModified(input.lastModified());
			input.delete();
			tempJar.renameTo(input);
		} catch (IOException e) {
			if (verbose) {
				System.out.println("Error normalizing jar " + input.getName()); //$NON-NLS-1$
				e.printStackTrace();
			}
		}
	}

	public String getStepName() {
		return "Sign"; //$NON-NLS-1$
	}
}
