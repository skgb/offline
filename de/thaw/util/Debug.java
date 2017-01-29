/* $Id$
 * 
 * Copyright (c) 2016 Arne Johannessen
 * All rights reserved. See LICENSE for details.
 */

package de.thaw.util;


public class Debug {
	
	// static only
	private Debug () {
	}
	
	public static String abbreviatedStackTrace (final Throwable throwable, final String myPackageLeader) {
		final int maxStackTraces = 12;
		
		final StringBuilder builder = new StringBuilder();
		builder.append( throwable.getClass().getCanonicalName() );
		if (throwable.getMessage() != null) {
			builder.append(":\n      ");
			builder.append(throwable.getMessage());
		}
		
		final StackTraceElement[] stack = throwable.getStackTrace();
		boolean myPackageFound = false;
		int i = 0;
		for ( ; i < stack.length && i < maxStackTraces; i++) {
			final String trace = stack[i].toString();
			builder.append("\n- ");
			builder.append(trace);
			if (myPackageFound && ! trace.startsWith(myPackageLeader)) {
				break;
			}
			if (! myPackageFound && trace.startsWith(myPackageLeader)) {
				myPackageFound = true;
			}
		}
		if (i < maxStackTraces - 1) {
			builder.append("\n… " + (stack.length - i - 1) + " more");
		}
		
		if (throwable.getCause() != null) {
			builder.append("\n\nCaused by:\n");
			builder.append(abbreviatedStackTrace(throwable.getCause(), myPackageLeader));
		}
		return builder.toString();
	}
	
}
