/* Copyright (c) 2017 Arne Johannessen
 * All rights reserved. See LICENSE for details.
 */

package de.skgb.offline.gui;

import java.awt.Desktop;
import java.net.URI;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;


/**
 * Online version check.
 */
class VersionCheck implements Runnable {
	
	
	private final String checkUri = "https://intern.skgb.de/skgb-offline/version";
	private final String updateUri = "https://intern.skgb.de/skgb-offline/update";
	
	private final Gui gui;
	
	private String currentVersion;
	
	
	VersionCheck (Gui gui) {
		this.gui = gui;
	}
	
	
	public void run () {
		int result = getCurrentVersion();
		if (currentVersion == null) {
			System.err.println("Version check was not successful (" + result + ")");
			// TODO: more obvious report if the last successful check was a long time ago
			return;
		}
		if (! currentVersion.equals(gui.app.version)) {
			SwingUtilities.invokeLater( new VersionMismatchDialog(gui.app.version, currentVersion) );
			return;
		}
		System.out.println("SKGB-offline " + currentVersion + " is the latest version");
	}
	
	
	private int getCurrentVersion () {
		int status = 0;
		currentVersion = null;
		try {
			HttpURLConnection connection = (HttpURLConnection) new URL(checkUri).openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Accept", "text/plain");
			connection.setInstanceFollowRedirects(true);
			connection.connect();
			status = connection.getResponseCode();
			if (status == HttpURLConnection.HTTP_OK) {
				BufferedReader reader = new BufferedReader( new InputStreamReader(connection.getInputStream()) );
				currentVersion = reader.readLine();
			}
			connection.disconnect();
		}
		catch (IOException e) {
			// ignore (e. g. network unavailable)
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		return status;
	}
	
	
	private class VersionMismatchDialog extends JOptionPane implements Runnable {
		
		String updateCaption = "Aktualisieren";
		String cancelCaption = "Nicht jetzt";
		
		VersionMismatchDialog (String running, String current) {
			super(new String[]{
				"<html>Es ist eine neue Version von SKGB-offline verfügbar (dies ist Version " + gui.app.version + ",<br>aktuell ist " + currentVersion + "). Du solltest die Website besuchen, um zu aktualisieren!</html>",
				"<html><small><br>" + updateUri + "</small></html>"
			});
			setOptions(new String[]{ updateCaption, cancelCaption });
			if (! Desktop.isDesktopSupported()) {
				setOptions(new String[]{"OK"});
			}
			setInitialValue( getOptions()[0] );
			setMessageType(JOptionPane.WARNING_MESSAGE);
		}
		
		boolean shouldUpdate () {
			JDialog dialog = createDialog(gui.window, "SKGB-offline: Update");
			dialog.setVisible(true);
			dialog.dispose();
			return updateCaption.equals(getValue());
		}
		
		public void run () {
			VersionMismatchDialog dialog = new VersionMismatchDialog(gui.app.version, currentVersion);
			boolean update = dialog.shouldUpdate();
			if (update) {
				try {
					Desktop.getDesktop().browse(URI.create(updateUri));
				}
				catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}
	
	
	/**
	 * Causes the version check to execute. A new thread is created and the
	 * Virtual Machine calls the run method of this object.
	 * 
	 * @throws SecurityException if the current thread can't modify this thread
	 * @throws IllegalThreadStateException if the thread was already started
	 */
	void start () {
		final Thread thread = new Thread(this);
		thread.setPriority(Thread.MIN_PRIORITY);
		thread.setUncaughtExceptionHandler(gui);
		thread.start();
	}
	
}
