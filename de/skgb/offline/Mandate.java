// $Id$

package de.skgb.offline;


import java.util.Collections;
import java.util.Map;


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
	 * Initialises this instance from a data table line read from a CSV file.
	 * This constructor is designed to work with a data row from a
	 * MutableCsvFile instance.
	 * @param mandate the mandate's data
	 * @throws NullPointerException if mandate == null
	 */
	Mandate (final Map<String, String> mandate) {
		// defensive copy may be unneeded right now because mandate is private
		properties = Collections.unmodifiableMap(mandate);
	}
	
	
	/**
	 * Unique Mandate Reference
	 * @return value for key "UMR" (or null if the key is missing)
	 */
	String uniqueReference () {
		return properties.get("UMR");
	}
	
	/**
	 * Mandatsdatum (Unterschrift)
	 * @return value for key "Signed" (or null if the key is missing)
	 */
	String signatureDate () {
		return properties.get("Signed");
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
	
}
