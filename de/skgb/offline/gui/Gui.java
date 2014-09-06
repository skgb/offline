// $Id$

package de.skgb.offline.gui;


import de.skgb.offline.DebitDataException;
import de.skgb.offline.SkgbOffline;

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
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
	static final String version = "0.3";
	
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
//		EventQueue.invokeLater( window );
		window.run();
		
		fileDialog = new CsvFileDialog(window);
		
		prefs = new Preferences().load();
		String path = prefs.get("MandateStore");
		if (path != null) {
			File file = new File(path);
			if (file != null && file.canRead()) {
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
		try {
			if (event.getSource() == window.mandateStoreButton) {
				File file = fileDialog.open("Mandatssammlung öffnen");
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
				
				File inFile = fileDialog.open("Lastschriftdatei öffnen");
				if (inFile == null) {
					return;
				}
				
				File outFile = fileDialog.save("Lastschriftdatei mit Kontodaten sichern", "out.csv");
				if (outFile == null) {
					return;
				}
				
//				try {
					app.process(inFile, outFile);
//				}
//				catch (IOException e) {
//					throw new RuntimeException(e);
//				}
				
//				System.out.println(outFile);
				
			}
			else {
				throw new UnsupportedOperationException();
			}
		}
		catch (Exception e) {
			reportException(e);
		}
	}
	
	private void reportException (final Exception exception) {
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
					String message = "Es ist ein Problem aufgetreten, möglicherweise wegen eines Programmierfehlers.\nBitte wende Dich an den IT-Ausschuss der SKGB.";
					if (exception instanceof DebitDataException) {
						message = "Die zuvor geöffnete Lastschriftdatei konnte nicht gelesen werden;\nsie könnte defekt sein. Bitte wende Dich an die SKGB-Geschäftsführung.\n\n_______\n(Die folgenden Angaben können der Fehlersuche dienen.)";
					}
					message += "\n\n" + abbreviatedStackTrace(exception);
					JOptionPane.showMessageDialog(window, message, "SKGB-offline: Fehler", JOptionPane.ERROR_MESSAGE);
				}
			});
		}
	}
	
	private static String abbreviatedStackTrace (final Throwable throwable) {
		final int maxStackTraces = 12;
		
		final StringBuilder builder = new StringBuilder();
		builder.append( throwable.getClass().getCanonicalName() );
		if (throwable.getMessage() != null) {
			builder.append(":\n      ");
			builder.append(throwable.getMessage());
		}
		
		final StackTraceElement[] stack = throwable.getStackTrace();
		boolean myPackageFound = false;
		int i = 0;
		for ( ; i < stack.length && i < maxStackTraces; i++) {
			final String trace = stack[i].toString();
			builder.append("\n- ");
			builder.append(trace);
			if (myPackageFound && ! trace.startsWith(myPackageLeader)) {
				break;
			}
			if (! myPackageFound && trace.startsWith(myPackageLeader)) {
				myPackageFound = true;
			}
		}
		if (i < maxStackTraces - 1) {
			builder.append("\n… " + (stack.length - i - 1) + " more");
		}
		
		if (throwable.getCause() != null) {
			builder.append("\n\nCaused by:\n");
			builder.append(abbreviatedStackTrace(throwable.getCause()));
		}
		return builder.toString();
	}
	
	public static void main (final String[] args) {
		new Gui();
	}
	
}
