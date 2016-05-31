// $Id$

package de.skgb.offline;


import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Represents a SEPA Core Direct Debit Mandate.
 * Instances of this class are immutable.
 */
final class Mandate {
	
	
	/**
	 * An immutable key-value map of this mandate's properties.
	 */
	final Map<String, String> properties;
	
	
	/**
	 * 
	 */
	final String commentKey;
	
	
	/**
	 * Initialises this instance from a data table line read from a CSV file.
	 * This constructor is designed to work with a data row from a
	 * MutableCsvFile instance.
	 * @param mandate the mandate's data
	 * @param commentKey the key used in the mandate data for the comments column (if any)
	 * @throws NullPointerException if mandate == null
	 */
	Mandate (final Map<String, String> mandate, String commentKey) {
		// defensive copy may be unneeded right now because mandate is private
		properties = Collections.unmodifiableMap(mandate);
		this.commentKey = commentKey == null ? "" : commentKey;
	}
	
	
	/**
	 * 
	 */
	void validate () {
		this.signatureDateAsDinOld();  // this call will trigger an exception if the signature date isn't valid
	}
	
	
	/**
	 * Unique Mandate Reference
	 * @return value for key "UMR" (or null if either the key is missing or the
	 *  value is the empty string, which is an error condition)
	 */
	String uniqueReference () {
		final String umr = properties.get("UMR");
		return umr == null || umr.length() == 0 ? null : umr;
	}
	
	/**
	 * Mandatsdatum (Unterschrift)
	 * @return original value for key "Signed" (or null if the key is missing)
	 */
	String signatureDate () {
		return properties.get("Signed");
	}
	
	
	/**
	 * 
	 */
	private static class SignatureDate {
		static final Pattern isoPattern = Pattern.compile("([0-9]{4})-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])");
		static final Pattern dinPattern = Pattern.compile("(0?[1-9]|[12][0-9]|3[01])\\. ?(0?[1-9]|1[012])\\. ?([0-9]{4})");
		final String year;
		final String month;
		final String day;
		SignatureDate (final String year, final String month, final String day) {
			this.year = year;
			this.month = month.length() == 1 ? "0" + month : month;
			this.day = day.length() == 1 ? "0" + day : day;
		}
		static SignatureDate parseIso (final String signatureDate) {
			final Matcher iso = isoPattern.matcher(signatureDate);
			if (iso.matches()) {
				return new SignatureDate(iso.group(1), iso.group(2), iso.group(3));
			}
			return null;
		}
		static SignatureDate parseDin (final String signatureDate) {
			final Matcher din = dinPattern.matcher(signatureDate);
			if (din.matches()) {
				return new SignatureDate(din.group(3), din.group(2), din.group(1));
			}
			return null;
		}
	}
	
	/**
	 * @return a java.util.regex.Matcher instance that contains the year in
	 *  group 1, the month in group 2 and the day in group 3
	 * @throws IllegalStateException if the key "Signed" is null or can't be
	 *  parsed as a date
	 */
	private SignatureDate signatureDateParsed () {
		final String signatureDate = signatureDate();
		if (signatureDate == null) {
			throw new MandateDataException("failed to parse empty signatureDate (UMR: '" + String.valueOf(uniqueReference()) + "')");
		}
		
		SignatureDate date = SignatureDate.parseIso(signatureDate);
		if (date == null) {
			date = SignatureDate.parseDin(signatureDate);
		}
		if (date == null) {
			throw new MandateDataException("failed to parse signatureDate '" + String.valueOf(signatureDate) + "' (UMR: '" + String.valueOf(uniqueReference()) + "')");
		}
		return date;
	}
	
	/**
	 * Mandatsdatum (Unterschrift), DIN-Format alt (31.12.1999)
	 * @return date value of key "Signed" expressed in legacy little-endian DIN
	 *  format or the empty string iff the value of key "Signed" is the empty
	 *  string
	 * @throws IllegalStateException if the value of key "Signed" is null or
	 *  can't be parsed as a date (unless it is the empty string)
	 */
	String signatureDateAsDinOld () {
		if (signatureDate().length() == 0) {
			return "";
		}
		SignatureDate date = signatureDateParsed();
		return date.day + "." + date.month + "." + date.year;
	}
	
	/**
	 * IBAN
	 * @return value for key "IBAN" (or null if the key is missing)
	 */
	String iban () {
		return properties.get("IBAN");
	}
	
	/**
	 * BIC
	 * @return value for key "SWIFT-BIC" (or null if the key is missing)
	 */
	String bic () {
		return properties.get("SWIFT-BIC");
	}
	
	/**
	 * Kontoinhaber
	 * @return value for key "Holder" (or null if the key is missing)
	 */
	String accountHolder () {
		return properties.get("Holder");
	}
	
	/**
	 * Kommentar
	 * @return value for comment from last column (or null if the comment wasn't parsed)
	 */
	String comment () {
		return properties.get(commentKey);
	}
	
}
