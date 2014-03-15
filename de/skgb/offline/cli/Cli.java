// $Id: $


// javac -cp lib/opencsv-2.3.jar -encoding UTF-8 Main.java
// java -cp lib/opencsv-2.3.jar:. Main

//package de.skgb.offline.cli;


import java.io.IOException;
import java.io.File;



public class Cli {
	
	public static void main(String[] args) throws IOException {
		final File mandateFile = new File("db.csv");
		final File debitInFile = new File("Adressenlosen-Ueberweisung.csv");
		final File debitOutFile = new File("a-out.csv");
		new SkgbOffline(mandateFile).process(debitInFile, debitOutFile);
	}
	
}

