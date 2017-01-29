/* $Id$
 * 
 * Copyright (c) 2014-2016 Arne Johannessen
 * All rights reserved. See LICENSE for details.
 */

package de.skgb.offline;


import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.xml.bind.DatatypeConverter;


/**
 * A collection of Mandates, initialised from a mandate store data table.
 * Instances of this class are immutable.
 */
public final class MandateStore {
	
	
	/** The date on which the mandate store was created; null if unkown. */
	public final String updated;
	
	/** Describes any error in loading the mandate store. */
	public final String error;
	
	/** true if the signature hash was missing. */
	public final boolean hashMissing;
	
	/** true if the signature hash was found and matches the one calculated from the actual data. */
	public final boolean hashMatches;
	
	/** The signature hash as calculated from the actual data. */
	final String hashBase64;
	
	/** The list of mandates. */
	final Collection<Mandate> mandates;
	
	
	/**
	 * Initialises this mandate store from a mandate store table in CSV format.
	 * <p>
	 * Only CSV records having an UMR are added to the mandate store.
	 * 
	 * @param csvFile the CSV table file to read the mandate store data from;
	 *  the given MutableCsvFile instance is no longer needed after this
	 *  MandateStore instance is constructed
	 */
	MandateStore (final MutableCsvFile csvFile) {
		error = csvFile.error;
		
		final String signature = csvFile.header.get(csvFile.header.size() - 1);
		
		final List<Mandate> mandates = new ArrayList<Mandate>( csvFile.data.size() );
		// :BUG: the mandates order isn't well-defined coming from MutableCsvFile, but we need that a constant order for hash comparison
		for (final Map<String, String> row : csvFile.data) {
			final Mandate mandate = new Mandate(row, signature);
			if (mandate.uniqueReference() != null) {
				// this row is not empty, so we use it
				mandates.add( mandate );
			}
		}
		this.mandates = Collections.unmodifiableCollection(mandates);
		
		final Matcher regex = Pattern.compile(".*updated ([0-9]{4}-(?:0[1-9]|1[012])-(?:0[1-9]|[12][0-9]|3[01])).*").matcher(signature);
		if (regex.matches()) {
			updated = regex.group(1);
		}
		else {
			updated = null;
		}
		final Matcher regex2 = Pattern.compile(".*[A-Za-z0-9+/]{22}==.*").matcher(signature);
		hashMissing = ! regex2.matches();
		
		hashBase64 = hashString(mandates, updated);
		hashMatches = signature.endsWith( hashBase64 );
	}
	
	
	/**
	 * Calculates the signature hash code used to verify the mandate store file
	 * hasn't been changed. This is not a cryptographic code, merely a simple
	 * checksum. The current implementation uses the MD5 algorithm.
	 * @param mandates the list to calculate the hash for; to be able to
	 *  compare the resulting hash-code, the list order needs to be
	 *  well-defined (e. g. in the order of the rows in the backing CSV file)
	 * @param updated the date-stamp to include in the hash
	 * @return the hash code
	 */
	static String hashString (final List<Mandate> mandates, final String updated) {
		final StringBuilder hashBuilder = new StringBuilder();
		for (final Mandate mandate : mandates) {
			for (final String cell : mandate.properties.values()) {
				hashBuilder.append(cell);
			}
		}
		hashBuilder.append(String.valueOf( updated ));  // valueOf avoids NullPointerException
		try {
			// macintosh charset: historical reasons
			final byte[] data = hashBuilder.toString().getBytes(Charset.forName("x-MacRoman"));
			final byte[] hash = MessageDigest.getInstance("MD5").digest(data);
			return DatatypeConverter.printBase64Binary(hash);
		}
		catch (NoSuchAlgorithmException e) {
			// this shouldn't happen as the MD5 algorithm is supposed to be built-in
			throw new RuntimeException(e);
		}
	}
	
	
	/**
	 * Finds and retrieves a single Mandate from the store.
	 * @param reference the unique mandate reference of the mandate to search for
	 * @return null if the mandate is not in the store
	 * @throws NullPointerException if reference == null
	 */
	Mandate byReference (final String reference) {
		// we don't have that many mandates => sequential search
		for (final Mandate mandate : mandates) {
			if (mandate == null) {
				throw new IllegalStateException("mandate store contains <null> value (searching for UMR: '" + reference + "')");
			}
			if ( reference.equals(mandate.uniqueReference()) ) {
				return mandate;
			}
		}
		return null;
	}
	
	
	/**
	 * The signature hash as calculated from the actual data.
	 * @return the value of the <code>hashBase64</code> field
	 */
	public String hash () {
		return hashBase64;
	}
	
	
	/**
	 * Tries to find any data errors in the mandates.
	 */
	public void validate () {
		for (final Mandate mandate : mandates) {
			if (mandate == null) {
				throw new IllegalStateException("mandate store contains <null> value");
			}
			mandate.validate();
		}
	}
	
	
	/**
	 * Debug output -- DO NOT USE in production code. Format subject to change.
	 */
	public String toString () {
		final StringBuilder b = new StringBuilder();
		b.append("updated: " + updated + "\n");
		b.append("error: " + error + "\n");
		b.append("hashMissing: " + hashMissing + "\n");
		b.append("hashMatches: " + hashMatches + "\n");
		b.append("hashBase64 (calculated): " + hashBase64 + "\n");
		b.append("mandates.size: " + mandates.size() + "\n");
		b.append("mandates: { ");
		for (final Mandate mandate : mandates) {
			b.append(mandate.uniqueReference() + " ");
		}
		b.append("}\n");
		return b.toString();
	}
	
}
