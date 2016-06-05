// $Id$

package de.thaw.util;

import java.io.File;
import java.net.URISyntaxException;


/**
 * Methods to obtain paths to standard OS directories.
 */
public class SystemDirectories { 
	
	
	// no instances
	private SystemDirectories () {
	}
	
	
	/**
	 * The directory in which the executable is located. This will be the JAR
	 * file's parent directory or the root directory for a class file tree.
	 * An OS X package will not be resolved; this method will return the Java
	 * Resource folder inside the package.
	 * <p>
	 * This method will only produce a valid result if this class is included
	 * in your application's main JAR file or class file tree, not if it is
	 * elsewhere in the class path.
	 * <p>
	 * Using this method with a class file tree has not yet been well tested.
	 * It is recommend to package your application as a JAR file.
	 * @return <code>null</code> if any problem occurs while determining the
	 *  path. In particular, <code>null</code> will be returned if a security
	 *  manager exists and its checkPermission method doesn't allow getting the
	 *  <code>ProtectionDomain</code> of this class; if the returned
	 *  <code>CodeSource</code> of this class happens to be <code>null</code>;
	 *  if converting the <code>CodeSource</code> to an URI and to a File fails
	 *  for any reason.
	 */
	public static File mainDir () {
		final Class aClass = SystemDirectories.class;
		try {
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
	
	
	/**
	 * The user's working directory.
	 * @return <code>null</code> if any problem occurs while determining the
	 *  path. In particular, <code>null</code> will be returned if a security
	 *  manager exists and its checkPropertyAccess method doesn't allow access
	 *  to the system property <code>user.dir</code> which specifies the working
	 *  dir or if it doesn't provide a usable result.
	 */
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
	
	
	/**
	 * The directory for per-user application-specific files on Windows
	 * (<code>%APPDATA%</code>). Determined by reading the <code>APPDATA</code>
	 * environment variable, which is only expected to contain a valid result
	 * on Windows. Do not depend on return values for this method on any other
	 * OS.
	 * @return <code>null</code> if any problem occurs while determining the
	 *  path. In particular, <code>null</code> will be returned if a security
	 *  manager exists and its checkPermission method doesn't allow access to
	 *  the environment variable <code>APPDATA</code> or if it doesn't provide
	 *  a usable result.
	 */
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
	
	
	/**
	 * The directory for user-specific and computer-specific application
	 * settings on Windows (<code>%LOCALAPPDATA%</code>). Determined by reading
	 * the <code>LOCALAPPDATA</code> environment variable, which is only
	 * expected to contain a valid result on Windows Vista and later. Do not
	 * depend on return values for this method on any other OS. For earlier
	 * Windows versions you should consider using winRoamingAppDataDir()
	 * instead.
	 * @return <code>null</code> if any problem occurs while determining the
	 *  path. In particular, <code>null</code> will be returned if a security
	 *  manager exists and its checkPermission method doesn't allow access to
	 *  the environment variable <code>LOCALAPPDATA</code> or if it doesn't
	 *  provide a usable result.
	 */
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
	
	
	/**
	 * The current user's profile directory on Windows
	 * (<code>%USERPROFILE%</code>). Determined by reading the
	 * <code>USERPROFILE</code> environment variable, which is only expected to
	 * contain a valid result on Windows NT and later. Do not depend on return
	 * values for this method on any other OS.
	 * @return <code>null</code> if any problem occurs while determining the
	 *  path. In particular, <code>null</code> will be returned if a security
	 *  manager exists and its checkPermission method doesn't allow access to
	 *  the environment variable <code>USERPROFILE</code> or if it doesn't
	 *  provide a usable result.
	 */
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
	
	
	/**
	 * The current user's home directory on Windows
	 * (<code>%HOMEDRIVE%%HOMEPATH%</code>). Determined by combining the values
	 * of the <code>HOMEDRIVE</code> and <code>HOMEPATH</code> environment
	 * variables, which are only expected to contain a valid result on Windows.
	 * Do not depend on return values for this method on any other OS.
	 * @return <code>null</code> if any problem occurs while determining the
	 *  path. In particular, <code>null</code> will be returned if a security
	 *  manager exists and its checkPermission method doesn't allow access to
	 *  both of these environment variables or if they do not provide a usable
	 *  result.
	 */
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
	
	
	/**
	 * The current user's home directory. This method uses a diverse approach
	 * to determine the current user's home dir on any OS.
	 * @return <code>null</code> if any problem occurs while determining the
	 *  path. In particular, <code>null</code> will be returned if a security
	 *  manager exists and doesn't allow access to the environment variables
	 *  and system properties required to determine the home dir or if these do
	 *  not provide a usable result.
	 */
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
	
	
	/**
	 * The path to the root Preferences folder on OS X (in <code>/Library</code>).
	 * @return <code>new File("/Library/Preferences")</code>
	 */
	public static File macRootPrefsDir () {
		// OS X localises just the user interface, not the file system paths
		return new File("/Library/Preferences");
	}
	
	
	/**
	 * The path to the user Preferences folder on OS X (in <code>~/Library</code>).
	 * @return a <code>File</code> pointing to ~/Library/Preferences
	 */
	public static File macUserPrefsDir () {
		final File homeDir = homeDir();
		if (homeDir == null) {
			return null;
		}
		// OS X localises just the user interface, not the file system paths
		return new File(homeDir, "Library/Preferences");
	}
	
	
	/**
	 * The path to a user config directory on Unix.
	 * @return a <code>File</code> pointing to ~/.config
	 */
	public static File unixConfigDir () {
		final File homeDir = homeDir();
		if (homeDir == null) {
			return null;
		}
		// un*x doesn't use a standardised preferences dir, but ~/.config prolly comes the closest
		return new File(homeDir, ".config");
	}
	
	
}
