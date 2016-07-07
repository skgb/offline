/* $Id$
 * 
 * Copyright (c) 2016 Arne Johannessen
 * All rights reserved. See LICENSE for details.
 */

package de.skgb.offline;


import java.io.IOException;
import java.io.File;


/**
 * Two-step processing for the merge operation.
 */
public final class SkgbOfflineProcessor {
	
	final SkgbOffline app;
	
	MutableCsvFile mergedFile;
	
	
	/**
	 * 
	 * @param app the SkgbOffline instance to use for processing
	 * @throws NullPointerException if app == null
	 */
	public SkgbOfflineProcessor (final SkgbOffline app) {
		if (app == null) {
			throw new NullPointerException("can't process without SkgbOffline instance");
		}
		this.app = app;
	}
	
	
	/**
	 * JOIN a CSV debit file with the mandate store. The file is directly read
	 * from the disk and the result is stored internally in this instance for
	 * later use by out().
	 * @param inFile the CSV debit file to read the input data table from
	 * @return this instance (enabling chaining)
	 * @throws NullPointerException if inFile == null
	 * @throws IOException .
	 * @throws DebitDataException if a semantic error is detected in the debit
	 *  data
	 */
	public SkgbOfflineProcessor in (final File inFile) throws IOException {
		mergedFile = app.merge( MutableCsvFile.read(inFile) );
		return this;
	}
	
	
	/**
	 * Writes the result of an earlier call to in() to the disk.
	 * @param outFile the CSV debit file to write the output data table to
	 *  (an existing file will be overwritten)
	 * @return this instance (enabling chaining)
	 * @throws IllegalStateException if in() hasn't been called yet
	 * @throws NullPointerException if outFile == null
	 * @throws IOException .
	 */
	public SkgbOfflineProcessor out (final File outFile) throws IOException {
		if (mergedFile == null) {
			throw new IllegalStateException("call in() first!");
		}
		mergedFile.write(outFile);
		return this;
	}
	
	
}
