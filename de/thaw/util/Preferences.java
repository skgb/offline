// $Id$

package de.thaw.util;


import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.LinkedList;


public class Preferences {
	
	protected final String bundleIdentifier;
	protected final String fileName;
	
	protected File file;
	protected Properties prefs;
	
	
	
	public Preferences (final String bundleIdentifier) {
		this.bundleIdentifier = bundleIdentifier;
		fileName = bundleIdentifier + ".properties";
		prefs = new Properties();
		findPrefsFile();
	}
	
	
	public Preferences set (final String key, final Object value) {
		prefs.setProperty(key, String.valueOf(value));
		return this;  // enable chaining
	}
	
	
	public String get (final String key) {
		return prefs.getProperty(key);
	}
	
	
	public void delete (final String key) {
		prefs.remove(key);
	}
	
	
	/**
	 * Attempt to determine the path to the preferences file, depending on the
	 * operating system and any existing prefs file.
	 */
	protected void findPrefsFile () {
		/*
		 * We're really searching for two different locations at the same time:
		 * - The first location in search order that we deem to be usable for
		 *   writing TO it (if necessary) [newFile], and
		 * - the first location in search order that actually contains a prefs
		 *   file we can read FROM [file].
		 * If [file] is not found, it remains at null. Default values are
		 * used. The [newFile] will only be used if save() is called while
		 * [file] == null.
		 *
		 *
		 * OS X [newFile] order:
		 *   userprefs
		 * OS X [file] order:
		 *   next to JAR file (if not bundled), next to app bundle, user Prefs, root prefs, user.dir
		 * Windows [newFile] order:
		 *   %APPDATA% (XP-DE = Anwendungsdaten), %LOCALAPPDATA% (fallback to keep out of user's home dir, vista+ only), home (USERPROFILE, HOMEDRIVE+HOMEPATH, user.home)
		 * Windows [file] order:
		 *   next to JAR file, %LOCALAPPDATA%, %APPDATA%, home, user.dir
		 * Unix [newFile] order:
		 *   ~/.config
		 * Unix [file] order:
		 *   next to JAR file, ~/.config, user.dir
		 *
		 *
		 * unified [newFile] order:
		 *   userprefs, %APPDATA% (XP-DE = Anwendungsdaten), %LOCALAPPDATA% (fallback to keep out of user's home dir, vista+ only), home (USERPROFILE, HOMEDRIVE+HOMEPATH, user.home - all win only), ~/.config
		 * unified [file] order:
		 *   next to JAR file (app bundle if bundled), user Prefs, root prefs, %LOCALAPPDATA%, %APPDATA%, home, ~/.config, user.dir
		 */
		
		final boolean win = String.valueOf( System.getProperty("os.name") ).contains("Windows");
		final boolean mac = String.valueOf( System.getProperty("os.name") ).contains("Mac OS X");
		
		final LinkedList<File> searchOrder = new LinkedList<File>();
		searchOrder.add( SystemDirectories.mainDir() );  // :BUG: if this app is bundled, we want to search next to the bundle instead of next to the JAR file on its inside
		searchOrder.add( mac ? SystemDirectories.macUserPrefsDir() : null );
		searchOrder.add( mac ? SystemDirectories.macRootPrefsDir() : null );
		searchOrder.add( win ? SystemDirectories.winLocalAppDataDir() : null );
		searchOrder.add( win ? SystemDirectories.winRoamingAppDataDir() : null );
		searchOrder.add( ! win ? SystemDirectories.unixConfigDir() : null );
		searchOrder.add( SystemDirectories.homeDir() );
		searchOrder.add( SystemDirectories.workingDir() );
		
		file = null;
		for (final File dir : searchOrder) {
			if (dir == null) {
				continue;
			}
			final File searchFile = new File(dir, fileName);
			if (searchFile.isFile() && searchFile.canRead()) {
				file = searchFile;
				break;  // we've found our prefs file
			}
		}
		
		if (file == null) {
			final LinkedList<File> createOrder = new LinkedList<File>();
			createOrder.add( mac ? SystemDirectories.macUserPrefsDir() : null );
			createOrder.add( win ? SystemDirectories.winRoamingAppDataDir() : null );
			createOrder.add( win ? SystemDirectories.winLocalAppDataDir() : null );
			createOrder.add( win ? SystemDirectories.homeDir() : null );
			createOrder.add( ! win ? SystemDirectories.unixConfigDir() : null );
			
			for (final File dir : createOrder) {
				if (dir == null) {
					continue;
				}
				File parent = dir;
				while (! parent.exists()) {
					parent = parent.getParentFile();
				}
				if (parent.isDirectory() && parent.canWrite()) {
					file = new File(dir, fileName);
					break;  // this is where our prefs file should be created
				}
			}
		}
	}
	
	
	public Preferences load () throws IOException {
		FileInputStream stream = null;
		final Properties properties = new Properties();
		try {
			stream = new FileInputStream(file);
			properties.load(stream);
			if (bundleIdentifier.equals(properties.getProperty("CFBundleIdentifier"))) {
				prefs = properties;
			}
			else {
				System.err.println("CFBundleIdentifier mismatch in prefs file " + file.toString());
			}
		}
		catch (FileNotFoundException e) {
			// ignore; normal condition
		}
		finally {
			close(stream);
		}
		return this;  // enable chaining
	}
	
	
	public void save () throws IOException {
		set("CFBundleIdentifier", bundleIdentifier);
		FileOutputStream stream = null;
		try {
			if (! file.getParentFile().exists()) {
				file.getParentFile().mkdirs();  // may fail
			}
			stream = new FileOutputStream(file);
			prefs.store(stream, " SKGB-offline Preferences");
		}
		finally {
			close(stream);
		}
	}
	
	
	protected static void close (Closeable stream) {
		if (stream == null) {
			return;  // nothing to close
		}
		try {
			stream.close();
		}
		catch (IOException e) {
			e.printStackTrace();  // closing a preferences stream is non-critical, so we just log the error
		}
	}
	
}
