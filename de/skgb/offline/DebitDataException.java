// $Id$

package de.skgb.offline;


/**
 * A semantic error with the debit data. Examples include mandate references
 * found in debit files that don't exist in the mandate store.
 */
public final class DebitDataException extends RuntimeException {
	
	DebitDataException (final String message) {
		super(message);
	}
	
	DebitDataException (final String message, final Throwable cause) {
		super(message, cause);
	}
	
}
