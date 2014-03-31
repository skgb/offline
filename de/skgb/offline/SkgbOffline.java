// $Id$

package de.skgb.offline;


import java.io.IOException;
import java.io.File;
import java.util.Map;
import java.util.TreeMap;


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
		debitHeader = new TreeMap<String, String>();
		debitHeader.put("accountHolder", "Empfängername");
		debitHeader.put("iban", "IBAN");
		debitHeader.put("bic", "BIC");
		debitHeader.put("jobType", "Auftragsart");
		debitHeader.put("creditorId", "GläubigerID");
		debitHeader.put("uniqueReference", "MandatsID");
		debitHeader.put("signatureDate", "Mandatsdatum");
		debitHeader.put("comment", "Kommentar");
	}
	
	
	/** SEPA CDD creditor ID of the SKGB */
	final static String creditorId = "DE67SEG00000074132";
	
	
	/** The mandate store, as initialised by the constructor. */
	public final MandateStore mandateStore;
	
	
	/** The file the mandateStore was initialised from, for later reference by the client. */
	public final File mandateFile;
	
	
	/**
	 * Initialises an instance with a mandate store read from a file.
	 * @param mandateFile the CSV file to read the mandate store from
	 * @throws NullPointerException if file == null
	 * @throws IOException
	 */
	public SkgbOffline (final File mandateFile) throws IOException {
		this.mandateFile = mandateFile;
		mandateStore = new MandateStore( MutableCsvFile.read(mandateFile) );
//		System.out.println(mandateStore.hashMissing);
//		System.out.println(mandateStore.hashMatches);
		System.out.println(mandateStore.hashBase64);
//		System.out.println(mandateStore.updated);
	}
	
	
	/**
	 * JOIN a CSV debit file with the mandate store. The file is directly read
	 * from the disk and the result is directly written to the disk.
	 * @param inFile the CSV debit file to read the input data table from
	 * @param outFile the CSV debit file to write the output data table to
	 *  (an existing file will be overwritten)
	 * @throws NullPointerException if inFile == null || outFile == null
	 * @throws IOException
	 * @throws DebitDataException
	 */
	public void process (final File inFile, final File outFile) throws IOException {
		merge( MutableCsvFile.read(inFile) ).write(outFile);
	}
	
	
	/**
	 * Adds SEPA CDD mandate data to the given CSV debit file. This method
	 * implements the actual JOIN operation.
	 * @throws NullPointerException if debitFile == null
	 * @throws DebitDataException
	 */
	MutableCsvFile merge (final MutableCsvFile debitFile) {
		for (final Map<String, String> debit : debitFile.data) {
			final String uniqueReference = debit.get(debitHeader.get("uniqueReference"));
			final Mandate mandate = mandateStore.byReference(uniqueReference);
			if (mandate == null) {
				throw new DebitDataException("mandate '" + uniqueReference + "' not found");
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
				debit.put(debitHeader.get("uniqueReference"), "");  // no mandate reference for a payment
				debit.put(debitHeader.get("comment"), "UMR " + uniqueReference);
			}
			else {
				throw new DebitDataException("job type '" + jobType + "' unknown");
			}
		}
		return debitFile;
	}
	
}