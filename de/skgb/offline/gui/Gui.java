// $Id$

package de.skgb.offline.gui;


import de.skgb.offline.SkgbOffline;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;


/**
 * The main class for running this app as GUI. This is a controller class,
 * intended to only contain glue code.
 */
class Gui implements ActionListener {
	
	/**
	 * The GUI version of this app. Note that this version number does not
	 * necessarily reflect updates to the backend in the de.skgb.offline
	 * package. At some point this field should be moved over there so that
	 * we have a grand unified version number for the whole project.
	 */
	static final String version = "0.1.1";
	
	SkgbOffline app;
	
	Preferences prefs;
	
	GuiWindow window;
	
	Gui () {
		window = new GuiWindow(this);
		window.versionLabel.setText("Version " + version);
//		EventQueue.invokeLater( window );
		window.run();
		
		prefs = new Preferences().load();
		String path = prefs.get("MandateStore");
		if (path != null) {
			File file = new File(path);
			if (file != null && file.isFile()) {
				loadMandateStore(file);
			}
		}
	}
	
	void loadMandateStore (File file) {
		try {
			app = new SkgbOffline(file);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		window.mandateStoreField.setText(file.toString());
		window.debitFileButton.setEnabled(! app.mandateStore.hashMissing);
		window.updateInfoPanel(app.mandateStore);
		
//		System.out.println(app.mandateStore.hashMissing);
//		System.out.println(app.mandateStore.hashMatches);
//		System.out.println(app.mandateStore.hashBase64);
//		System.out.println(app.mandateStore.updated);
	}
	
	public void savePrefs () {
		prefs.set("MandateStore", app.mandateFile.toString());
		prefs.save();
	}
	
	public void actionPerformed (ActionEvent event) {
		if (event.getSource() == window.mandateStoreButton) {
			File file = new CsvFileDialog(window).open("Mandatssammlung öffnen");
			if (file == null) {
				return;
			}
			loadMandateStore(file);
			savePrefs();
			
		}
		else if (event.getSource() == window.debitFileButton) {
			if (app == null) {
				throw new IllegalStateException();
			}
			
			File inFile = new CsvFileDialog(window).open("Lastschriftdatei öffnen");
			if (inFile == null) {
				return;
			}
			
			File outFile = new CsvFileDialog(window).save("Lastschriftdatei mit Kontodaten sichern", "out.csv");
			if (outFile == null) {
				return;
			}
			
			try {
				app.process(inFile, outFile);
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}
			
//			System.out.println(outFile);
//			throw new UnsupportedOperationException();
			
		}
		else {
			throw new UnsupportedOperationException();
		}
	}
	
	public static void main (final String[] args) {
		new Gui();
	}
	
}
