// $Id: $


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;


class Preferences {
	
	static final String bundleIdentifier = "de.skgb.offline";
	
	File file, newFile;
	Properties prefs;
	
	Preferences () {
		/* We're really searching for two different locations at the same time:
		 * - The first location in search order that we deem to be usable for
		 *   writing TO it (if necessary) [newFile], and
		 * - the first location in search order that actually contains a prefs
		 *   file we can read FROM [file].
		 * If [file] is not found, it remains at null. Default values are
		 * used. The [newFile] will only be used if save() is called while
		 * [file] == null.
		 */
		
		// http://en.wikipedia.org/wiki/Environment_variable#Default_Values_on_Microsoft_Windows
		// http://programmers.stackexchange.com/questions/3956/best-way-to-save-application-settings
		// http://bugs.java.com/bugdatabase/view%5Fbug.do?bug%5Fid=4787931
		
		// OS X: ~/Library/Preferences/
		// Unix: ~/.config/
		
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
		prefs = new Properties();
	}
	
	Preferences set (String key, Object value) {
		prefs.setProperty(key, String.valueOf(value));
		return this;  // enable chaining
	}
	
	String get (String key) {
		return prefs.getProperty(key);
	}
	
	Preferences load () {
		try {
			FileInputStream stream = new FileInputStream(file);
			Properties properties = new Properties();
			properties.load(stream);
			stream.close();
			if (! bundleIdentifier.equals(properties.getProperty("CFBundleIdentifier"))) {
				System.out.println("CFBundleIdentifier mismatch in prefs file " + file.toString());
				return this;
			}
			prefs = properties;
		}
		catch (FileNotFoundException e) {
			// ignore; normal condition
		}
		catch (IOException e) {
			e.printStackTrace();  // preferences are a non-critical function, so we just log the error
		}
		return this;  // enable chaining
	}
	
	void save () {
		set("CFBundleIdentifier", bundleIdentifier);
		try {
			FileOutputStream stream = new FileOutputStream(file);
			prefs.store(stream, "comment");
			stream.close();
		}
		catch (IOException e) {
			e.printStackTrace();  // preferences are a non-critical function, so we just log the error
		}
	}
	
}
