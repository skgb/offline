// $Id$

package de.skgb.offline.gui;


import de.thaw.util.SystemDirectories;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.LinkedList;


class Preferences {
	
	static final String bundleIdentifier = "de.skgb.offline";
	static final String prefVersion = "1";
	static final String fileName = bundleIdentifier + ".properties";
	
	File file;
	Properties prefs;
	
	
	
	Preferences () {
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
		* 
		* - construct folder path
		* - 
		*
		
		*
		* next to JAR file:
		* new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile()  // may fail, but prolly won't (provided there's a JAR file)
		*
		
		*
		* getProtectionDomain - SecurityException
		* toURI - URISyntaxException
		*
//		try {
		
		boolean win = String.valueOf( System.getProperty("os.name") ).contains("Windows");
		boolean mac = String.valueOf( System.getProperty("os.name") ).contains("Mac OS X");
		
		LinkedList<File> searchOrder = new LinkedList<File>();
		searchOrder.add( SystemDirectories.mainDir() );  // :BUG: if this app is bundled, we want to search next to the bundle instead of next to the JAR file on its inside
		searchOrder.add( mac ? SystemDirectories.macUserPrefsDir() : null );
		searchOrder.add( mac ? SystemDirectories.macRootPrefsDir() : null );
		searchOrder.add( win ? SystemDirectories.winLocalAppDataDir() : null );
		searchOrder.add( win ? SystemDirectories.winRoamingAppDataDir() : null );
		searchOrder.add( ! win ? SystemDirectories.unixConfigDir() : null );
		searchOrder.add( SystemDirectories.homeDir() );
		searchOrder.add( SystemDirectories.workingDir() );
		
		LinkedList<File> createOrder = new LinkedList<File>();
		createOrder.add( mac ? SystemDirectories.macUserPrefsDir() : null );
		createOrder.add( win ? SystemDirectories.winRoamingAppDataDir() : null );
		createOrder.add( win ? SystemDirectories.winLocalAppDataDir() : null );
		createOrder.add( win ? SystemDirectories.homeDir() : null );
		createOrder.add( ! win ? SystemDirectories.unixConfigDir() : null );
		
		System.out.println( searchOrder );
		System.out.println( createOrder );
		System.out.println( "------------------" );
		
		for (File searchDir : searchOrder) {
			if (searchDir == null) {
				continue;
			}
			File searchFile = new File(searchDir, fileName);
			if (searchFile.isFile() && searchFile.canRead()) {
				file = searchFile;
				break;  // we've found our prefs file
			}
		}
		System.out.println( file );
		
		if (file == null) {
			for (File createDir : createOrder) {
				if (createDir == null) {
					continue;
				}
				File createDirParent = createDir;
				while (! createDirParent.exists()) {
					createDirParent = createDirParent.getParentFile();
				}
				if (createDirParent.isDirectory() && createDirParent.canWrite()) {
					file = new File(createDir, fileName);
					break;  // this is where our prefs file should be created
				}
			}
		}
		System.out.println( file );
//		file = new File("/Users/aj3/a/b/c/d");
//		System.out.println( file );
		System.out.println( "------------------" );
		
		
		System.out.println( SystemDirectories.mainDir() );
		System.out.println( SystemDirectories.winUserProfileHomeDir() );
		System.out.println( SystemDirectories.winDrivePathHomeDir() );
		System.out.println( SystemDirectories.homeDir() );
		System.out.println( SystemDirectories.macRootPrefsDir() );
		System.out.println( SystemDirectories.macUserPrefsDir() );
		System.out.println( SystemDirectories.unixConfigDir() );
		System.out.println( SystemDirectories.winRoamingAppDataDir() );
		System.out.println( SystemDirectories.winLocalAppDataDir() );
		System.out.println( SystemDirectories.workingDir() );
		
		
//		} catch (Exception e) { throw new Error(e); }
		
		*
		* OS X:
		* home = user.home || HOME
		* userprefs = home + "/Library/Preferences"
		* rootprefs = "/Library/Preferences"
		*
		
		*
		* Windows:
		* 
		*
		
		// http://en.wikipedia.org/wiki/Environment_variable#Default_Values_on_Microsoft_Windows
		// http://programmers.stackexchange.com/questions/3956/best-way-to-save-application-settings
		// http://bugs.java.com/bugdatabase/view%5Fbug.do?bug%5Fid=4787931
		
		// OS X: ~/Library/Preferences/
		// Unix: ~/.config/
// wie Pfad erzeugen, wenn er nicht existiert? oder einfach überspringen und zur nächsten option? im normalfall müsste das ja alles existieren
		
//		System.out.println(new File(String.valueOf( System.getenv("APPDATA") )));  // nicht Mac
//		System.out.println(new File(String.valueOf( System.getenv("USERPROFILE") )));  // nicht Mac, but definitely usable for home dir in Win
//		System.out.println(new File(String.valueOf( System.getenv("HOMEDRIVE") + System.getenv("HOMEPATH") )));  // nicht Mac
//		System.out.println(new File(String.valueOf( System.getenv("LOCALAPPDATA") )));  // nicht Mac + Win XP
//		System.out.println(new File(String.valueOf( System.getProperty("user.home") )));  // discouraged on Win (unreliable)
//		System.out.println(new File(String.valueOf( System.getenv("HOME") )));  // nicht Win
//		System.out.println(new File(String.valueOf( System.getProperty("os.name") )));  // "Windows XP" / "Mac OS X"
//		System.out.println(new File(String.valueOf( System.getProperty("os.arch") )));
//		System.out.println(new File(String.valueOf( System.getProperty("os.version") )));
//		System.out.println(new File("").getAbsoluteFile());
//		System.out.println(new File(String.valueOf( System.getProperty("user.dir") )));
		
		// wie Pfad erzeugen, wenn er nicht existiert? oder einfach überspringen und zur nächsten option? im normalfall müsste das ja alles existieren
		
		String s = System.getProperty("file.separator");
		String prefsPath = System.getProperty("user.home") + s + "Library" + s + "Preferences" + s + bundleIdentifier + ".properties";
		file = new File(prefsPath);
*/
		prefs = new Properties();
		findPrefsFile();
	}
	
	
	Preferences set (String key, Object value) {
		prefs.setProperty(key, String.valueOf(value));
		return this;  // enable chaining
	}
	
	
	String get (String key) {
		return prefs.getProperty(key);
	}
	
	
	void findPrefsFile () {
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
//		System.out.println( searchOrder );
		
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
//		System.out.println( file );
		
		if (file == null) {
			final LinkedList<File> createOrder = new LinkedList<File>();
			createOrder.add( mac ? SystemDirectories.macUserPrefsDir() : null );
			createOrder.add( win ? SystemDirectories.winRoamingAppDataDir() : null );
			createOrder.add( win ? SystemDirectories.winLocalAppDataDir() : null );
			createOrder.add( win ? SystemDirectories.homeDir() : null );
			createOrder.add( ! win ? SystemDirectories.unixConfigDir() : null );
//			System.out.println( createOrder );
			
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
//			System.out.println( file );
		}
	}
	
	
	Preferences load () {
		FileInputStream stream = null;
		final Properties properties = new Properties();
		try {
			stream = new FileInputStream(file);
			properties.load(stream);
			if (bundleIdentifier.equals(properties.getProperty("CFBundleIdentifier"))) {
				prefs = properties;
			}
			else {
				System.out.println("CFBundleIdentifier mismatch in prefs file " + file.toString());
			}
		}
		catch (FileNotFoundException e) {
			// ignore; normal condition
		}
		catch (IOException e) {
			e.printStackTrace();  // preferences are a non-critical function, so we just log the error
		}
		finally {
			close(stream);
		}
		return this;  // enable chaining
	}
	
	
	void save () {
		set("CFBundleIdentifier", bundleIdentifier);
		set("PreferencesVersion", prefVersion);
		FileOutputStream stream = null;
		try {
			if (! file.getParentFile().exists()) {
				file.getParentFile().mkdirs();  // may fail
			}
			stream = new FileOutputStream(file);
			prefs.store(stream, " SKGB-offline Preferences");
		}
		catch (IOException e) {
			e.printStackTrace();  // preferences are a non-critical function, so we just log the error
		}
		finally {
			close(stream);
		}
	}
	
	
	private static void close (Closeable stream) {
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
