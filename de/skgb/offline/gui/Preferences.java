// $Id$

package de.skgb.offline.gui;


import java.io.File;
import java.io.IOException;


/**
 * Implements user preferences for the SKGB-offline GUI.
 */
class Preferences extends de.thaw.util.Preferences {
	
	/** An identifier used to validate the preferences file. */
	static final String bundleIdentifier = "de.skgb.offline";
//	static final String prefVersion = "1";
	
	static final String MANDATE_STORE_KEY = "MandateStore";
	static final String DEBIT_FILE_FOLDER_KEY = "LastDebitFileDirectory";
	
	
	/**
	 * Load the preferences from disk. If the preferences file doesn't exist or
	 * is unreadable or incomplete, default values are used.
	 */
	Preferences () {
		super(bundleIdentifier);
		load();
	}
	
	
	/**
	 * Load the preferences from disk. If the preferences file doesn't exist or
	 * is unreadable or incomplete, default values are used.
	 * <p>
	 * Clients shouldn't need to call this method, as preferences are
	 * automatically loaded in the constructor.
	 * @return this object
	 */
	public Preferences load () {
		try {
			super.load();
		}
		catch (IOException e) {
			e.printStackTrace();  // preferences are a non-critical function, so we just log the error
		}
		return this;  // enable chaining
	}
	
	
	/**
	 * Save the preferences to disk.
	 * <p>
	 * Clients only need to ever call this method if the
	 * {@link de.thaw.util.Preferences#set(String,Object)} method is called
	 * directly, which is discouraged. This class's convenience methods to
	 * access preferences settings automatically save to disk each time they're
	 * called, so clients shouldn't need to call this method.
	 */
	public void save () {
//		super.prefs.setProperty("PreferencesVersion", prefVersion);
		try {
			super.save();
		}
		catch (IOException e) {
			e.printStackTrace();  // preferences are a non-critical function, so we just log the error
		}
	}
	
	
	/**
	 * The last-used mandate store file.
	 * @return <code>null</code> as default value if preference not set
	 */
	File mandateStore () {
		final String path = get(MANDATE_STORE_KEY);
		if (path == null) {
			return null;
		}
		File file = new File(path);
		if (! file.canRead()) {
			return null;
		}
		return file;
	}
	
	
	/**
	 * The last-used mandate store file's enclosing folder (convenience method).
	 * @return <code>null</code> as default value if preference not set
	 */
	String mandateStoreFolder () {
		File file = mandateStore();
		if (file == null) {
			return null;
		}
		return file.getParent();
	}
	
	
	/**
	 * The last-used mandate store file.
	 * Saves the preferences to disk.
	 * @param mandateFile .
	 */
	void mandateStore (final File mandateFile) {
		set(MANDATE_STORE_KEY, mandateFile.toString());
		save();
	}
	
	
	/**
	 * The last-used folder in the open debit file chooser dialog.
	 * @return <code>null</code> as default value if preference not set
	 */
	String debitFileFolder () {
		return get(DEBIT_FILE_FOLDER_KEY);
	}
	
	
	/**
	 * The last-used folder in the open debit file chooser dialog.
	 * Saves the preferences to disk.
	 * @param path .
	 */
	void debitFileFolder (final String path) {
		set(DEBIT_FILE_FOLDER_KEY, path);
		save();
	}
	
	
}
