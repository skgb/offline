/* $Id$
 * 
 * Copyright (c) 2014-2016 Arne Johannessen
 * All rights reserved. See LICENSE for details.
 */

package de.skgb.offline;


/**
 * A semantic error with the debit data. Examples include mandate references
 * found in debit files that don't exist in the mandate store.
 */
public class DebitDataException extends RuntimeException {
	
	DebitDataException (final String message) {
		super(message);
	}
	
	DebitDataException (final String message, final Throwable cause) {
		super(message, cause);
	}
	
}
