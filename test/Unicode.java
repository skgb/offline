package test;

import de.skgb.offline.*;
import java.io.*;


public class Unicode {
	
	public static void main (String[] args) throws IOException {
		String inPath = args[0];
		String outPath = args[1];
		
		PrintWriter writer = new PrintWriter(outPath + "/test3.log.txt", "x-MacRoman");
		
		writeMandateStoreInfo(writer, inPath + "/mandates-utf8-bom.csv");
		writeMandateStoreInfo(writer, inPath + "/mandates-utf8-nobom.csv");
		
		final File mandateFile = new File(inPath + "/mandates-utf8-bom.csv");
		final File debitInFile = new File(inPath + "/debit-latin1.csv");
		final File debitOutFile = new File(outPath + "/test3-latin1.out.csv");
		
		new SkgbOfflineProcessor( new SkgbOffline(mandateFile) ).in(debitInFile).out(debitOutFile);
		
		writer.close();
	}
	
	
	static void writeMandateStoreInfo (PrintWriter writer, String path) throws IOException {
		writer.print("Loading mandate store: " + path + "\n");
		writer.flush();  // we want this out early in case there is an exception in the next step
		try {
			writer.print( new SkgbOffline(new File(path)).mandateStore + "\n");
		}
		catch (Exception e) {
			writer.print( e + "\n\n" );
		}
		writer.flush();
	}
	
}
