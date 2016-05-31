// $Id$

package de.skgb.offline.gui;


import java.awt.Frame;
import java.awt.FileDialog;
import java.io.File;
import java.io.FilenameFilter;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;


/**
 * Standard OS open and save dialogs for CSV files.
 */
class CsvFileDialog {
	
	
	static boolean isWindows = String.valueOf( System.getProperty("os.name") ).contains("Windows");
	
	
	static class Filter extends FileFilter implements FilenameFilter {
		
		static final String extension = ".csv";
		
		public boolean accept (File dir, String name) {  // AWT
			return hasExtension(String.valueOf(name));
		}
		
		public boolean accept (File file) {  // Swing
			if (file.isDirectory()) {
				return true;
				// the file system may not be navigable if this returns false
				// that dirs are not selectable with Save is something this filter's client needs to handle (which it does by means of setFileSelectionMode's default FILES_ONLY)
			}
			return hasExtension(String.valueOf(file));
		}
		
		static boolean hasExtension (String filename) {
			int lengthProper = filename.length() - extension.length();
			return lengthProper > 0 && filename.substring(lengthProper).equalsIgnoreCase( extension );
		}
		
		public String getDescription () {
			return "Lastschriftdatei (*" + extension + ")";
		}
		
	}
	
	
	class SaveCsvFileChooser extends JFileChooser {
		
		class ReplaceFileDialog extends JOptionPane {
			
			String replaceCaption = "Ersetzen";
			String cancelCaption = "Abbrechen";
			
			ReplaceFileDialog (File file) {
				super(new String[]{
					"<html><b>»" + file.getName() + "« existiert bereits.<br>Möchten Sie das Objekt ersetzen?</b></html>",
					"<html><small>Es existiert bereits eine Datei oder ein Ordner mit<br>demselben Namen " +
					(file.getParentFile().getName().length() > 0 ? "im Ordner " + file.getParentFile().getName() : "in diesem Ordner") +
					".<br>Beim Ersetzen wird der Inhalt überschrieben.</small></html>"
				});
				setOptions(new String[]{ replaceCaption, cancelCaption });
				setInitialValue( getOptions()[1] );
				setMessageType(JOptionPane.WARNING_MESSAGE);
			}
			
			boolean shouldReplace () {
				JDialog dialog = createDialog(SaveCsvFileChooser.this, title);
				dialog.setVisible(true);
				dialog.dispose();
				return replaceCaption.equals(getValue());
			}
			
		}
		
		Frame parent;
		Filter csvFilter = new Filter();
		String title;
		
		SaveCsvFileChooser (Frame parent, String title) {
			super(lastOpenPath);
			this.parent = parent;
			this.title = title;
			addChoosableFileFilter(csvFilter);
			setFileFilter(csvFilter);
			setDialogType(JFileChooser.SAVE_DIALOG);
			setDialogTitle(title);
		}
		
		@Override
		public void approveSelection () {
			File file = getSelectedFile();
			if (file == null) {
				return;
			}
			String name = file.getName();
			
			// fix extension
			if (getFileFilter() == csvFilter && ! Filter.hasExtension(name)) {
				name += Filter.extension;
				file = new File(file.getParentFile(), name);
				setSelectedFile(file);
			}
			
			if (file.exists() && ! new ReplaceFileDialog(file).shouldReplace() ) {
				return;  // we don't approve
			}
			super.approveSelection();  // we do approve
		}
		
		File show (String suggestedName) {
			setSelectedFile(new File(suggestedName));
			int result = showDialog(parent, null);;
			if (result != JFileChooser.APPROVE_OPTION) {
				return null;
			}
			return getSelectedFile();
		}
		
	}
	
	
	final Frame parent;
	String lastOpenPath = null;
	
	
	CsvFileDialog (final Frame parent) {
		this.parent = parent;
	}
	
	
	File open (String title, String path) {
		FileDialog dialog = new FileDialog(parent, title, FileDialog.LOAD);
		if (path != null && path.length() > 0) {
			dialog.setDirectory(path);
		}
		dialog.setFilenameFilter(new Filter());
		dialog.setVisible(true);
		if (dialog.getFile() == null || dialog.getDirectory() == null) {
			return null;
		}
		lastOpenPath = dialog.getDirectory();
		return new File(dialog.getDirectory() + dialog.getFile());
	}
	
	
	File save (String title, String suggestedName) {
		if (isWindows) {
			SaveCsvFileChooser saveDialog = new SaveCsvFileChooser(parent, title);
			return saveDialog.show(suggestedName);
			// the Win native AWT dialog isn't too great, see #124
			// OTOH the Mac OS X laf's Swing dialog is abysmal
		}
		
		FileDialog dialog = new FileDialog(parent, title, FileDialog.SAVE);
		dialog.setFile(suggestedName);  // side effect on OS X if this has an extension (e. g. "a.csv"): enables button "hide extension"
		dialog.setVisible(true);
		if (dialog.getFile() == null || dialog.getDirectory() == null) {
			return null;
		}
		return new File(dialog.getDirectory() + dialog.getFile());
	}
	
}
