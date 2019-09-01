/* $Id$
 * 
 * Copyright (c) 2014-2016 Arne Johannessen
 * All rights reserved. See LICENSE for details.
 */

package de.skgb.offline;


import java.io.IOException;
import java.io.File;
import java.util.Map;
import java.util.TreeMap;
import java.util.Collections;


/**
 * The main class of the SKGB-offline application.
 * Instances of this class do not contain mutable data.
 */
public final class SkgbOffline {
	
	// some constants pertaining to the WISO Mein Geld CSV debit file format
	final static String directDebitJob = "SEPA-LAS";
	final static String paymentJob = "SEPA-ÜB";
	final static Map<String, String> debitHeader;
	static {
		final Map<String, String> map = new TreeMap<String, String>();
		map.put("accountHolder", "Empfängername");
		map.put("iban", "IBAN");
		map.put("bic", "BIC");
		map.put("jobType", "Auftragsart");
		map.put("creditorId", "GläubigerID");
		map.put("uniqueReference", "MandatsID");
		map.put("signatureDate", "Mandatsdatum");
		map.put("comment", "Kommentar");
		debitHeader = Collections.unmodifiableMap(map);
	}
	
	
	/**
	 * The grand unified version number of this app.
	 */
	public static final String version = "0.6.5";
	
	
	/** SEPA CDD creditor ID of the SKGB */
	static final String creditorId = "DE67SEG00000074132";
	
	
	/** The mandate store, as initialised by the constructor. */
	public final MandateStore mandateStore;
	
	
	/** The file the mandateStore was initialised from, for later reference by the client. */
	public final File mandateFile;
	
	
	/**
	 * Initialises an instance with a mandate store read from a file.
	 * @param mandateFile the CSV file to read the mandate store from
	 * @throws NullPointerException if file == null
	 * @throws IOException .
	 */
	public SkgbOffline (final File mandateFile) throws IOException {
		this.mandateFile = mandateFile;
		mandateStore = new MandateStore( MutableCsvFile.read(mandateFile) );
	}
	
	
	/**
	 * JOIN a CSV debit file with the mandate store. The file is directly read
	 * from the disk and the result is directly written to the disk.
	 * @param inFile the CSV debit file to read the input data table from
	 * @param outFile the CSV debit file to write the output data table to
	 *  (an existing file will be overwritten)
	 * @throws NullPointerException if inFile == null || outFile == null
	 * @throws IOException .
	 * @throws DebitDataException if a semantic error is detected in the debit
	 *  data
	 */
	public void process (final File inFile, final File outFile) throws IOException {
		merge( MutableCsvFile.read(inFile) ).write(outFile);
	}
	
	
	/**
	 * Adds SEPA CDD mandate data to the given CSV debit file. This method
	 * implements the actual JOIN operation.
	 * @param debitFile the CSV file containing the direct debit data that is
	 *  still missing the mandate data
	 * @return the same MutableCsvFile instance passed as debitFile (allowing
	 *  call chaining)
	 * @throws NullPointerException if debitFile == null
	 * @throws DebitDataException if a semantic error is detected in the debit
	 *  data
	 */
	MutableCsvFile merge (final MutableCsvFile debitFile) {
		for (final Map<String, String> debit : debitFile.data) {
			final String uniqueReference = debit.get(debitHeader.get("uniqueReference"));
			Mandate mandate = null;
			try {
				mandate = mandateStore.byReference(uniqueReference);
			}
			catch (NullPointerException exception) {
				if (! debit.containsKey(debitHeader.get("uniqueReference"))) {
					final String line = String.valueOf(debit);
					throw new DebitDataException("UMR header '" + debitHeader.get("uniqueReference") + "' not found in debit job " + (line.length() > 80 ? line.substring(0, 80) + "…" : line), exception);
				}
				throw exception;
			}
			if (mandate == null) {
				throw new NoMandateException(uniqueReference);
			}
			
			String comment = debit.get(debitHeader.get("comment")).trim();
			if (mandate.comment() != null && mandate.comment().trim().length() > 0) {
				if (comment.length() > 0) {
					comment += " / ";
				}
				comment += mandate.comment();
			}
			debit.put(debitHeader.get("iban"), mandate.iban());
			debit.put(debitHeader.get("bic"), mandate.bic());
			debit.put(debitHeader.get("accountHolder"), mandate.accountHolder());
			final String jobType = debit.get(debitHeader.get("jobType"));
			if (jobType.equals( directDebitJob )) {
				// NB: these assignments will fail silently if the headers aren't spelled EXACTLY right
				debit.put(debitHeader.get("signatureDate"), mandate.signatureDateAsDinOld());
				debit.put(debitHeader.get("creditorId"), creditorId);
			}
			else if (jobType.equals( paymentJob )) {
				// no mandate reference for a payment
				debit.put(debitHeader.get("uniqueReference"), "");
				if (comment.length() > 0) {
					comment += " / ";
				}
				comment += "UMR " + uniqueReference;
			}
			else {
				throw new DebitDataException("job type '" + jobType + "' unknown");
			}
			if (comment.length() > 0) {
				debit.put(debitHeader.get("comment"), comment);
			}
		}
		return debitFile;
	}
	
}
