// $Id$


import de.skgb.offline.*;
import java.io.*;


public class Test1 {
	
	public static void main (String[] args) throws IOException {
		String inPath = args[0];
		String outPath = args[1];
		
		PrintWriter writer = new PrintWriter(outPath + "/log.txt", "x-MacRoman");
		
		writeMandateStoreInfo(writer, inPath + "/mandates-valid-empty.csv");
		writeMandateStoreInfo(writer, inPath + "/mandates-valid.csv");  // -hashok-withdate
		writeMandateStoreInfo(writer, inPath + "/mandates-hashok-nodate.csv");
		writeMandateStoreInfo(writer, inPath + "/mandates-nohash-withdate.csv");
		writeMandateStoreInfo(writer, inPath + "/mandates-nohash-nodate.csv");
		writeMandateStoreInfo(writer, inPath + "/mandates-wronghash-withdate.csv");
		writeMandateStoreInfo(writer, inPath + "/mandates-wronghash-nodate.csv");
		
		final File mandateFile = new File(inPath + "/mandates-valid.csv");
		final File debitInFile = new File(inPath + "/debit-simplified.csv");
		final File debitOutFile = new File(outPath + "/out.csv");
		
		SkgbOffline app = new SkgbOffline(mandateFile);
		app.process(debitInFile, debitOutFile);
		
		writer.close();
	}
	
	
	static void writeMandateStoreInfo (PrintWriter writer, String path) throws IOException {
		writer.print("Loading mandate store: " + path + "\n");
		writer.flush();  // we want this out early in case there is an exception in the next step
		writer.print( new SkgbOffline(new File(path)).mandateStore + "\n");
		writer.flush();
	}
	
}
