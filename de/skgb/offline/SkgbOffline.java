// $Id$

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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;


final class MutableCsvFile {
	
	final static String charset = "windows-1252";
	final static char separator = ';';
	final static char quote = '"';
	final static String lineEnding = "\r\n";
	
	Collection<Map<String, String>> data = null;
	
	List<String> header = null;
	
	static MutableCsvFile read (final File file) throws IOException {
		final Reader fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), charset));
		final CSVReader csvReader = new CSVReader( fileReader, separator, quote );
		
		final String[] csvHeader = csvReader.readNext();
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
				row.put(csvHeader[i], csvData[i]);
			}
			table.add( row );
		}
		
		csvReader.close();
		final MutableCsvFile instance = new MutableCsvFile();
		instance.header = Arrays.asList(csvHeader);
		instance.data = table;
		return instance;
	}
	
	void write (final File file) throws IOException {
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
	
	public String toString () {
		final StringBuilder builder = new StringBuilder();
		for (final Map<String, String> map : data) {
			for (final Map.Entry<String, String> entry : map.entrySet()) {
				builder.append(entry.getKey() + "=" + entry.getValue() + ", ");
			}
			builder.append("\n");
		}
		return builder.toString();
	}
	
}


final class Mandate {
	private final Map<String, String> mandate;
	
	Mandate (final Map<String, String> mandate) {
		this.mandate = Collections.unmodifiableMap(mandate);
	}
	
	String uniqueReference () { return mandate.get("UMR"); }
	String signatureDate () { return mandate.get("Signed"); }
	String iban () { return mandate.get("IBAN"); }
	String bic () { return mandate.get("SWIFT-BIC"); }
	String accountHolder () { return mandate.get("Holder"); }
}


public final class SkgbOffline {
	
	final static String creditorId = "DE67SEG00000074132";
	
	final static String directDebitJob = "SEPA-LAS";
	final static String paymentJob = "SEPA-ÜB";
	
	final static Map<String, String> debitHeader;
	static {
		debitHeader = new TreeMap<String, String>();
		debitHeader.put("accountHolder", "Empfängername");
		debitHeader.put("iban", "IBAN");
		debitHeader.put("bic", "BIC");
		debitHeader.put("jobType", "Auftragsart");
		debitHeader.put("creditorId", "GläubigerID");
		debitHeader.put("uniqueReference", "MandatsID");
		debitHeader.put("signatureDate", "Mandatsdatum");
	}
	
	public final MandateStore mandateStore;
	
	public final File mandateFile;
	
	public SkgbOffline (final File mandateFile) throws IOException {
		this.mandateFile = mandateFile;
		mandateStore = new MandateStore( MutableCsvFile.read(mandateFile) );
//		System.out.println(mandateStore.hashMissing);
//		System.out.println(mandateStore.hashMatches);
		System.out.println(mandateStore.hashBase64);
//		System.out.println(mandateStore.updated);
	}
	
	public void process (final File inFile, final File outFile) throws IOException {
		merge( MutableCsvFile.read(inFile) ).write(outFile);
	}
	
	MutableCsvFile merge (final MutableCsvFile debitFile) {
		for (final Map<String, String> debit : debitFile.data) {
			final String uniqueReference = debit.get(debitHeader.get("uniqueReference"));
			final Mandate mandate = mandateStore.byReference(uniqueReference);
			if (mandate == null) {
				throw new DebitDataException("mandate '" + uniqueReference + "' not found");
			}
			
			debit.put(debitHeader.get("iban"), mandate.iban());
			debit.put(debitHeader.get("bic"), mandate.bic());
			final String jobType = debit.get(debitHeader.get("jobType"));
			if (jobType.equals( directDebitJob )) {
				// NB: these assignments will fail silently if the headers aren't spelled EXACTLY right
				debit.put(debitHeader.get("accountHolder"), mandate.accountHolder());
				debit.put(debitHeader.get("signatureDate"), mandate.signatureDate());
				debit.put(debitHeader.get("creditorId"), creditorId);
			}
			else if (jobType.equals( paymentJob )) {
				debit.put(debitHeader.get("uniqueReference"), "");  // no mandate reference for a payment
			}
			else {
				throw new DebitDataException("job type '" + jobType + "' unknown");
			}
		}
		return debitFile;
	}
	
}


class DebitDataException extends RuntimeException {
	DebitDataException (final String message) {
		super(message);
	}
}
