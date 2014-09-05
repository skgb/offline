// $Id: SystemDirectories.java 2014-09 aj3 $

package de.thaw.util;

import java.io.File;
import java.net.URISyntaxException;


/**
*/
public class SystemDirectories { 
	
	
	public static File mainDir () {
//		Class aClass = getClass();
		Class aClass = SystemDirectories.class;
		try {
			// :BUG: works for JAR file - but how about direct class exec?
			return new File( aClass.getProtectionDomain().getCodeSource().getLocation().toURI() ).getParentFile();
		}
		catch (SecurityException e) {  // getProtectionDomain()
			// fall through
		}
		catch (NullPointerException e) {  // getCodeSource() may be null
			// fall through
		}
		catch (URISyntaxException e) {  // toURI()
			// fall through
		}
		catch (IllegalArgumentException e) {  // new File(URI)
			// fall through
		}
		return null;
	}
	
	
	public static File workingDir () {
		try {
			if (System.getProperty("user.dir") != null) {
				return new File( System.getProperty("user.dir") );
			}
		}
		catch (SecurityException e) {
			// fall through
		}
		return null;
	}
	
	
	public static File winRoamingAppDataDir () {
		try {
			if (System.getenv("APPDATA") != null) {
				return new File( System.getenv("APPDATA") );
			}
		}
		catch (SecurityException e) {
			// fall through
		}
		return null;
	}
	
	
	public static File winLocalAppDataDir () {
		try {
			if (System.getenv("LOCALAPPDATA") != null) {
				return new File( System.getenv("LOCALAPPDATA") );
			}
		}
		catch (SecurityException e) {
			// fall through
		}
		return null;
	}
	
	
	public static File winUserProfileHomeDir () {
		try {
			if (System.getenv("USERPROFILE") != null) {
				return new File( System.getenv("USERPROFILE") );
			}
		}
		catch (SecurityException e) {
			// fall through
		}
		return null;
	}
	
	
	public static File winDrivePathHomeDir () {
		try {
			if (System.getenv("HOMEDRIVE") != null && System.getenv("HOMEPATH") != null) {
				return new File( System.getenv("HOMEDRIVE") + System.getenv("HOMEPATH") );
			}
		}
		catch (SecurityException e) {
			// fall through
		}
		return null;
	}
	
	
	public static File homeDir () {
		boolean win = false;
		try {
			win = String.valueOf( System.getProperty("os.name") ).contains("Windows");
		}
		catch (SecurityException e) {
			// default to skip the Windows-only paths
		}
		try {
			File homeDir = null;
			if (win) {
				homeDir = winUserProfileHomeDir();
				if (homeDir != null && homeDir.isDirectory()) {
					return homeDir;
				}
				homeDir = winDrivePathHomeDir();
				if (homeDir != null && homeDir.isDirectory()) {
					return homeDir;
				}
			}
			if (System.getProperty("user.home") != null) {
				homeDir = new File( System.getProperty("user.home") );
				if (homeDir.isDirectory()) {
					return homeDir;
				}
			}
			if (System.getenv("HOME") != null) {
				homeDir = new File( System.getenv("HOME") );
				if (homeDir.isDirectory()) {
					return homeDir;
				}
			}
		}
		catch (SecurityException e) {
			// fall through
		}
		return null;
	}
	
	
	public static File macRootPrefsDir () {
		return new File("/Library/Preferences");
	}
	
	
	public static File macUserPrefsDir () {
		File homeDir = homeDir();
		if (homeDir == null) {
			return null;
		}
		return new File(homeDir, "Library/Preferences");
	}
	
	
	public static File unixConfigDir () {
		File homeDir = homeDir();
		if (homeDir == null) {
			return null;
		}
		return new File(homeDir, ".config");
	}
	
	
}
