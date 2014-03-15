// $Id$


// javac -cp lib/opencsv-2.3.jar -encoding UTF-8 Main.java
// java -cp lib/opencsv-2.3.jar:. Main

package de.skgb.offline.cli;


import de.skgb.offline.SkgbOffline;

import java.io.IOException;
import java.io.File;



/**
 * The main class for running this app as CLI.
 * @deprecated This used to be a debugging driver. It seems superfluous at this
 *  point and could be removed soon.
 */
@Deprecated
public class Cli {
	
	public static void main(String[] args) throws IOException {
		final File mandateFile = new File("db.csv");
		final File debitInFile = new File("Adressenlosen-Ueberweisung.csv");
		final File debitOutFile = new File("a-out.csv");
		new SkgbOffline(mandateFile).process(debitInFile, debitOutFile);
	}
	
}

