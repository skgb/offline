// $Id$

//package de.skgb.offline.gui;


import java.awt.Frame;
import java.awt.FileDialog;
import java.io.File;
import java.io.FilenameFilter;


/**
 * Standard OS open and save dialogs for CSV files.
 */
class CsvFileDialog {
	
	
	class Filter implements FilenameFilter {
		public boolean accept (File dir, String name) {
			return name.length() > 4 && name.substring(name.length() - 4).equalsIgnoreCase(".csv");
		}
	}
	
	
	Frame parent;
	
	
	CsvFileDialog (Frame parent) {
		this.parent = parent;
	}
	
	
	File open (String title) {
		FileDialog dialog = new FileDialog(parent, title, FileDialog.LOAD);
		dialog.setFilenameFilter(new Filter());
		dialog.setVisible(true);
		if (dialog.getFile() == null || dialog.getDirectory() == null) {
			return null;
		}
		return new File(dialog.getDirectory() + dialog.getFile());
	}
	
	
	File save (String title, String suggestedName) {
		FileDialog dialog = new FileDialog(parent, title, FileDialog.SAVE);
		dialog.setFile(suggestedName);  // side effect on OS X if this has an extension (e. g. "a.csv"): enables button "hide extension"
		dialog.setVisible(true);
		if (dialog.getFile() == null || dialog.getDirectory() == null) {
			return null;
		}
		return new File(dialog.getDirectory() + dialog.getFile());
	}
	
}
