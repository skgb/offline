// $Id$

package de.skgb.offline.gui;


import de.skgb.offline.DebitDataException;
import de.skgb.offline.MandateDataException;
import de.skgb.offline.NoMandateException;
import de.skgb.offline.SkgbOffline;
import de.skgb.offline.SkgbOfflineProcessor;
import de.thaw.util.Debug;

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

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
	static final String version = "0.5.1";
	
	// used for stack trace abbreviation
	private static final String myPackageLeader = "de.skgb.";
	
	SkgbOffline app;
	
	Preferences prefs;
	
	GuiWindow window;
	
	CsvFileDialog fileDialog;
	
	Gui () {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception e) {
			// ignore (keep default LAF)
		}
		
		window = new GuiWindow(this);
		window.versionLabel.setText("Version " + version);
		window.run();
		
		prefs = new Preferences();
		File file = prefs.mandateStore();
		if (file != null) {
			// try to load the mandate store given in the preferences; if that fails, just move on and leave the user to load it manually
			try {
				loadMandateStore(file);
			}
			catch (MandateDataException e) {
				e.printStackTrace();
			}
		}
		
		fileDialog = new CsvFileDialog(window);
	}
	
	void loadMandateStore (File file) {
		try {
			SkgbOffline app = new SkgbOffline(file);
			app.mandateStore.validate();
			this.app = app;
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		window.mandateStoreField.setText(file.toString());
		window.debitFileButton.setEnabled(! app.mandateStore.hashMissing && app.mandateStore.error == null);
		window.updateInfoPanel(app.mandateStore);
		
//		System.out.println(app.mandateStore.hashMissing);
//		System.out.println(app.mandateStore.hashMatches);
//		System.out.println(app.mandateStore.hashBase64);
//		System.out.println(app.mandateStore.updated);
	}
	
	public void actionPerformed (ActionEvent event) {
		try {
			if (event.getSource() == window.mandateStoreButton) {
				String path = prefs.mandateStoreFolder();
				File file = fileDialog.open("Mandatssammlung öffnen", path);
				if (file == null) {
					return;
				}
				loadMandateStore(file);
				prefs.mandateStore(app.mandateFile);
				
			}
			else if (event.getSource() == window.debitFileButton) {
				if (app == null) {
					throw new IllegalStateException();
				}
				
				String path = prefs.debitFileFolder();
				File inFile = fileDialog.open("Lastschriftdatei öffnen", path);
				if (inFile == null) {
					return;
				}
				prefs.debitFileFolder(inFile.getParent());
				
				SkgbOfflineProcessor processor = new SkgbOfflineProcessor(app).in(inFile);
				
				String outFileName = inFile.getName() != null ? inFile.getName() : "out.csv";
				File outFile = fileDialog.save("Lastschriftdatei mit Kontodaten sichern", outFileName);
				if (outFile == null) {
					return;
				}
				
				processor.out(outFile);
//				app.process(inFile, outFile);
				
			}
			else if (event.getSource() instanceof WindowEvent && event.getActionCommand() == "close") {
				System.exit(0); // expedite (debug)
			}
			else {
				throw new UnsupportedOperationException();
			}
		}
		catch (NoMandateException e) {
			reportException(e, "Buchen nicht möglich\n\nDie Lastschriftdatei enthält eine Lastschrift für das Mandat '" + e.uniqueReference() + "',\nwelches nicht in der Mandatssammlung vom " + app.mandateStore.updated + " ist.");
		}
		catch (Exception e) {
			reportException(e);
		}
	}
	
	private void reportException (final Exception exception) {
		reportException(exception, null);
	}
	
	private void reportException (final Exception exception, final String message) {
		exception.printStackTrace();
		System.out.println();
		
		if (! GraphicsEnvironment.isHeadless()) {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			}
			catch (Exception e) {
				// ignore (keep default LAF)
			}
			SwingUtilities.invokeLater( new Runnable () {
				public void run () {
					String text = "Es ist ein Problem aufgetreten, möglicherweise wegen eines Programmierfehlers.\nBitte wende Dich an den IT-Ausschuss der SKGB.";
//					if (exception instanceof DebitDataException) {
//						text = "Die zuvor geöffnete Lastschriftdatei konnte nicht gelesen werden;\nsie könnte defekt sein. Bitte wende Dich an die SKGB-Geschäftsführung.";
//					}
					text += "\n\n_______\n(Die folgenden Angaben können der Fehlersuche dienen.)\n\n" + Debug.abbreviatedStackTrace(exception, myPackageLeader);
					if (message != null) {
						text = message;
					}
					JOptionPane.showMessageDialog(window, text, "SKGB-offline: Fehler", JOptionPane.ERROR_MESSAGE);
				}
			});
		}
	}
	
	public static void main (final String[] args) {
		new Gui();
	}
	
}
