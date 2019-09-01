/* Copyright (c) 2019 Arne Johannessen
 * All rights reserved. See LICENSE for details.
 */

package de.skgb.offline.gui;

import de.thaw.util.Debug;

import java.awt.Desktop;
import java.awt.GraphicsEnvironment;
import java.net.URI;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.IOException;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;


/**
 * Reports issues locally and remotely. The issue specified in the constructor
 * is automatically reported at the time of object creation. Local reporting
 * happens in the console and in the GUI, but remote reporting via email is
 * also attempted.
 */
class IssueReport implements Runnable {
	
	// used for stack trace abbreviation
	private static final String myPackageLeader = "de.skgb.";
	
	private final String reportUri = "https://intern.skgb.de/skgb-offline/exception";
	
	private final Gui gui;
	
	private final Throwable exception;
	
	private final String message;
	
	
	IssueReport (Gui gui, Throwable e) {
		this(gui, e, "Es ist ein Problem aufgetreten, möglicherweise wegen eines Programmierfehlers.\nBitte wende Dich an den IT-Ausschuss der SKGB.");
	}
	
	
	IssueReport (Gui gui, Throwable e, String message) {
		this.gui = gui;
		this.exception = e;
		this.message = message;
		start();
	}
	
	
	/**
	 * Causes the issue report to execute. A new thread is created and
	 * the Virtual Machine calls the run method of this object (for remote
	 * reporting).
	 */
	private void start () {
		// always report on console
		exception.printStackTrace();
		System.out.println();
		
		// attempt remote report 
		try {
			Thread thread = new Thread(this);
			thread.setPriority(Thread.MIN_PRIORITY);
			thread.start();
		}
		catch (Exception e) {
			remoteReportFailed(e);
		}
		
		// attempt GUI report
		if (! GraphicsEnvironment.isHeadless() && gui != null) {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			}
			catch (Exception e) {
				// ignore (keep default LAF)
			}
			final String guiText = message + "\n\n_______\n(Die folgenden Angaben können der Fehlersuche dienen.)\n\n" + Debug.abbreviatedStackTrace(exception, myPackageLeader);
			SwingUtilities.invokeLater( new Runnable () {
				public void run () {
					JOptionPane.showMessageDialog(gui.window, guiText, "SKGB-offline: Fehler", JOptionPane.ERROR_MESSAGE);
				}
			});
		}
	}
	
	
	public void run () {
		remoteReportSend();
	}
	
	
	private void remoteReportSend () {
		try {
			HttpURLConnection connection = (HttpURLConnection) new URL(reportUri).openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "text/plain;charset=UTF-8");
			connection.setRequestProperty("X-Java-Exception", exception.toString());
			connection.setDoOutput(true);
			connection.connect();
			BufferedWriter writer = new BufferedWriter( new OutputStreamWriter(connection.getOutputStream(), "UTF-8") );
			writer.append(message);
			writer.append("\n\nSoftware versions:");
			writer.append("\nSKGB-offline " + gui.app.version);
			writer.append("\nJava " + System.getProperty("java.version"));
			writer.append("\n\n");
			writer.append(Debug.stackTrace(exception));
			writer.append("\n\n(end of issue report)\n");
			writer.close();
			System.out.println(connection.getResponseCode());
			connection.disconnect();
		}
		catch (Exception e) {
			remoteReportFailed(e);
		}
	}
	
	
	private void remoteReportFailed (Throwable e) {
		System.out.println("Remote report failed:");
		e.printStackTrace();
		System.out.println();
	}
	
}
