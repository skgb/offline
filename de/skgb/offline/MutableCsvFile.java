/* $Id$
 * 
 * Copyright (c) 2014-2016 Arne Johannessen
 * All rights reserved. See LICENSE for details.
 */

package de.skgb.offline;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;


/**
 * A basic data table with unique headers, backed by a CSV file. This class is
 * specifically designed with the SKGB-offline app in mind. It will need to be
 * adapted for use in other applications.
 * <p>
 * This class does not guarantee any specific order of data rows. However, the
 * order will generally be well-defined by the backing Collection type.
 * <p>
 * Instances of this class are mutable to facilitate easy random access;
 * clients should make defensive copies as necessary.
 * 
 * @see au.com.bytecode.opencsv
 */
final class MutableCsvFile {
	
	final private static String charset = "windows-1252";
	final private static char separator = ';';
	final private static char quote = '"';
	final private static String lineEnding = "\r\n";
	final private static int minColCount = 5;  // fail if fewer columns
	final private static String tooFewColsMessage = "weniger Datenspalten als die erwarteten " + minColCount + " oder mehr";
	
	
	/**
	 * The actual data table. Headers are always unique in this implementation,
	 * so we use a map for easy access. Note that the order of rows or columns
	 * is undefined.
	 * <p>
	 * Clients that modify the data must make sure the header list is in sync.
	 */
	Collection<Map<String, String>> data = null;
	
	
	/**
	 * A list of all headers present in the data table.
	 * <p>
	 * Clients that modify the header list must make sure the data is in sync.
	 */
	List<String> header = null;
	
	
	/** Describes any syntactic problem in loading the file. */
	static String error = null;
	
	
	/**
	 * Determines whether the file can be decoded as UTF-8. If that is the
	 * case, it is more than likely that it is in fact UTF-8 (because of the
	 * encoding's structure).
	 * 
	 * @param file the CSV file to consider
	 * @return true if the file represents a valid UTF-8 byte stream
	 * @throws NullPointerException if file == null
	 * @throws IOException .
	 */
	static boolean isUtf8 (final File file) throws IOException {
		final Charset utf8 = Charset.forName("UTF-8");
		final FileInputStream stream = new FileInputStream(file);
		// this is a bit wasteful on memory, but it doesn't really matter for us
		final byte[] input = new byte[ (int) stream.getChannel().size() ];
		stream.read(input);
		final String decoded = new String(input, utf8);
		final byte[] output = decoded.getBytes(utf8);
		return Arrays.equals(input, output);
	}
	
	
	/**
	 * Read data from disk.
	 * <p>
	 * The column order of the CSV file will be reflected in the header field.
	 * 
	 * @param file the CSV file to read data from
	 * @return a new instance with the CSV file's data
	 * @throws NullPointerException if file == null
	 * @throws IOException .
	 * @see CSVReader
	 */
	static MutableCsvFile read (final File file) throws IOException {
		String readCharset = isUtf8(file) ? "UTF-8" : charset;
		
		Reader fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), readCharset));
		CSVReader csvReader = new CSVReader( fileReader, separator, quote );
		
		String[] csvHeader = csvReader.readNext();
		
		// hack to work around #147
		if (csvHeader.length < minColCount) {
			// try another separator if there seem to be fewer columns than expected
			// (parsing with wrong separator may dump everything into column 1)
			csvReader.close();
			fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), readCharset));
			csvReader = new CSVReader( fileReader, ',', quote );  // RFC 4180
			csvHeader = csvReader.readNext();
		}
		error = csvHeader.length < minColCount ? tooFewColsMessage : null;  // report if still too few
		
		// remove BOM
		if (csvHeader[0].charAt(0) == '\ufeff') {
			csvHeader[0] = csvHeader[0].substring(1);
		}
		
		for (int i = 0; i < csvHeader.length; i++) {
			csvHeader[i] = csvHeader[i].intern();
		}
		final LinkedList<Map<String, String>> table = new LinkedList<Map<String, String>>();
		String[] csvData = null;
		while ((csvData = csvReader.readNext()) != null) {
			final TreeMap<String, String> row = new TreeMap<String, String>();
			for (int i = 0; i < csvData.length && i < csvHeader.length; i++) {
				if (row.containsKey(csvHeader[i])) {
					throw new RuntimeException("key doppelt vorhanden");
				}
				String csvCell = csvData[i].trim();  // fix #149
				if (csvCell.length() == 0 && csvData[i].length() > 0) {
					csvCell = " ";  // certain software requires a non-empty last column; this line ensures cells containing just whitespace won't be empty
				}
				row.put(csvHeader[i], csvCell);
			}
			table.add( row );
		}
		
		csvReader.close();
		final MutableCsvFile instance = new MutableCsvFile();
		instance.header = Arrays.asList(csvHeader);
		instance.data = table;
		return instance;
	}
	
	
	/**
	 * Write the current state of this instance to disk. The behaviour is
	 * undefined if the data and header fields of this instance are out of
	 * sync. It is also undefined for null values in the data.
	 * <p>
	 * The column order in the CSV file will match that of the header field.
	 * 
	 * @param file the CSV file to write data to (existing file will be
	 *  overwritten)
	 * @throws NullPointerException if file == null
	 * @throws IllegalStateException if header == null || data == null
	 * @throws IOException .
	 * @see CSVWriter
	 */
	void write (final File file) throws IOException {
		if (data == null || header == null) {
			throw new IllegalStateException("data and header must be defined before attempting file output");
		}
		
		final Writer fileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), charset));
		final CSVWriter writer = new CSVWriter( fileWriter, separator, quote, lineEnding );
		
		writer.writeNext( header.toArray(new String[0]) );
		for (final Map<String, String> row : data) {
			final String[] csvData = new String[header.size()];
			for (int i = 0; i < header.size(); i++) {
				csvData[i] = row.get(header.get(i));
			}
			writer.writeNext( csvData );
		}
		writer.close();
	}
	
	
	/**
	 * Simplistic serialisation intended for debugging output.
	 * @return representation of the table data
	 *  (as key-value pairs, not in CSV format; the header list is not used)
	 */
	public String toString () {
		if (data == null) {
			return "null";
		}
		final StringBuilder builder = new StringBuilder();
		for (final Map<String, String> map : data) {
			// TODO: map actually has a usable toString method we might consider
			for (final Map.Entry<String, String> entry : map.entrySet()) {
//				if (entry.getValue() != null && entry.getValue().length() > 0) {
					builder.append(entry.getKey() + "=" + entry.getValue() + ", ");
//				}
			}
			builder.append("\n");
		}
		return builder.toString();
	}
	
}
