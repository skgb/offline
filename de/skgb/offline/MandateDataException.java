/* $Id$
 * 
 * Copyright (c) 2016 Arne Johannessen
 * All rights reserved. See LICENSE for details.
 */

package de.skgb.offline;


/**
 * A semantic error with the mandate store. Examples include mandate signature
 * dates that aren't parseable.
 */
public final class MandateDataException extends RuntimeException {
	
	MandateDataException (final String message) {
		super(message);
	}
	
	MandateDataException (final String message, final Throwable cause) {
		super(message, cause);
	}
	
}
