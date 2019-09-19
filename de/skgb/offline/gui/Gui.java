/* $Id$
 * 
 * Copyright (c) 2014-2016 Arne Johannessen
 * All rights reserved. See LICENSE for details.
 */

package de.skgb.offline.gui;


import de.skgb.offline.DebitDataException;
import de.skgb.offline.MandateDataException;
import de.skgb.offline.NoMandateException;
import de.skgb.offline.SkgbOffline;
import de.skgb.offline.SkgbOfflineProcessor;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.JTextArea;
import javax.swing.UIManager;

/**
 * The main class for running this app as GUI. This is a controller class,
 * intended to only contain glue code.
 */
class Gui implements ActionListener, Thread.UncaughtExceptionHandler {
	
	SkgbOffline app;
	
	Preferences prefs;
	
	GuiWindow window;
	
	CsvFileDialog fileDialog;
	
	boolean development = false;  // used to disable certain features during development
	
	
	Gui () {
		System.setProperty("com.apple.mrj.application.apple.menu.about.name", "SKGB-offline");  // shouldn't be necessary in Java 1.7+
		System.setProperty("apple.awt.application.name", "SKGB-offline");
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception e) {
			// ignore (keep default LAF)
		}
		prefs = new Preferences();
		
		window = new GuiWindow(this);
		if (prefs.advancedOptions()) {
			window.addAdvancedMenu(this);
		}
		window.versionLabel.setText("Version " + SkgbOffline.version);
		window.run();
		
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
		
		new VersionCheck(this).start();
		
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
	}
	
	
	void processDebitFile () throws IOException {
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
		outFileName = Pattern.compile("(?: Buchung [0-9]+)?\\.csv$").matcher(outFileName).replaceFirst(" Bankdaten.csv");
		File outFile = fileDialog.save("Lastschriftdatei mit Kontodaten sichern", outFileName);
		if (outFile == null) {
			return;
		}
		
		processor.out(outFile);
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
				processDebitFile();
				
			}
			else if (event.getSource() instanceof WindowEvent && event.getActionCommand() == "close" || event.getSource() == window.closeMenuItem) {
				window.dispose();
				System.exit(0); // expedite (debug)
				
			}
			else if (event.getSource() == window.advancedMenuItems.get("hash")) {
				JTextArea text = new JTextArea(app != null ? app.mandateStore.hash() : null);
				text.setEditable(false);
				JOptionPane.showMessageDialog(window, text, "Hash", JOptionPane.INFORMATION_MESSAGE);
			}
			else if (event.getSource() instanceof MouseEvent && event.getActionCommand() == "click") {
				MouseEvent mouseEvent = (MouseEvent)event.getSource();
				if (mouseEvent.getSource() == window.versionLabel && mouseEvent.isAltDown() && window.isMac) {  // this functionality is not required on other OSs right now
					throw new UnsupportedOperationException("'Erweitert'-Menü benutzen!");
				}
				
			}
			else {
				throw new UnsupportedOperationException();
			}
		}
		catch (NoMandateException e) {
			String message = "Buchen nicht möglich\n\nDie Lastschriftdatei enthält eine Lastschrift für das Mandat '" + e.uniqueReference() + "',\nwelches nicht in der Mandatssammlung vom " + app.mandateStore.updated + " ist.";
			new IssueReport(this, e, message);
		}
		catch (Exception e) {
			new IssueReport(this, e);
		}
	}
	
	
	public void uncaughtException (Thread t, Throwable e) {
		new IssueReport(this, e);
	}
	
	
	public static void main (final String[] args) {
		Gui gui = new Gui();
		
		if (args != null && args.length > 0 && "dev".equals(args[0])) {
			gui.development = true;
		}
	}
	
}
