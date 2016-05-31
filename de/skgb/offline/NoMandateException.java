// $Id: NoMandateException.java $

package de.skgb.offline;


/**
 * A semantic error with the debit data. Examples include mandate references
 * found in debit files that don't exist in the mandate store.
 */
public final class NoMandateException extends DebitDataException {
	
	final String reference;
	
	NoMandateException (final String uniqueReference) {
		super("mandate '" + uniqueReference + "' not found");
		reference = uniqueReference;
	}
	
	public String uniqueReference () {
		return reference;
	}
	
}
