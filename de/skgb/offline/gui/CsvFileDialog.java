/* $Id$
 * 
 * Copyright (c) 2014-2016 Arne Johannessen
 * All rights reserved. See LICENSE for details.
 */

package de.skgb.offline.gui;

import de.thaw.java.FileDialogs;

import java.awt.Frame;
import java.io.File;


/**
 * Standard OS open and save dialogs for CSV files.
 */
class CsvFileDialog extends FileDialogs {
	
	
	static FileDialogs.Filter filter = new CsvFilter();
	
	
	static class CsvFilter extends FileDialogs.Filter {
		
		static final String extension = ".csv";
		
		CsvFilter () {
			super(extension);
		}
		
		public String getDescription () {
			return "Lastschriftdatei (*" + extension + ")";
		}
		
	}
	
	
	CsvFileDialog (final Frame parent) {
		super(parent);
	}
	
	
	File open (String title, String path) {
		return open(title, path, filter);
	}
	
	
	File save (String title, String suggestedName) {
		return save(title, suggestedName, filter);
	}
	
}
