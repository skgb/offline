// $Id$

package de.skgb.offline;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.xml.bind.DatatypeConverter;


public final class MandateStore {
	
	public final String updated;
	
	public final boolean hashMissing;
	
	public final boolean hashMatches;
	
	final String hashBase64;
	
	final Collection<Mandate> mandates;
	
	MandateStore (final MutableCsvFile csvFile) {
		final Collection<Mandate> mandates = new ArrayList<Mandate>( csvFile.data.size() );
		for (final Map<String, String> row : csvFile.data) {
			mandates.add( new Mandate(row) );
		}
		this.mandates = Collections.unmodifiableCollection(mandates);
		
		final String signature = csvFile.header.get(csvFile.header.size() - 1);
		final Matcher regex = Pattern.compile(".*updated ([0-9]{4}-(?:0[1-9]|1[012])-(?:0[1-9]|[12][0-9]|3[01])).*").matcher(signature);
		if (regex.matches()) {
			updated = regex.group(1);
		}
		else {
			updated = null;
		}
		
		final Matcher regex2 = Pattern.compile(".*[A-Za-z0-9+/]{22}==.*").matcher(signature);
		hashMissing = ! regex2.matches();
		
		final StringBuilder hashBuilder = new StringBuilder();
		for (final Map<String, String> row : csvFile.data) {
			for (final String cell : row.values()) {
				hashBuilder.append(cell);
			}
		}
		hashBuilder.append(String.valueOf( this.updated ));
		try {
			final byte[] hash = MessageDigest.getInstance("MD5").digest( hashBuilder.toString().getBytes() );
			hashBase64 = DatatypeConverter.printBase64Binary(hash);
		}
		catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
		hashMatches = signature.endsWith( hashBase64 );
	}
	
	Mandate byReference (final String reference) {
		for (final Mandate mandate : mandates) {
			if ( reference.equals(mandate.uniqueReference()) ) {
				return mandate;
			}
		}
		return null;
	}
}
