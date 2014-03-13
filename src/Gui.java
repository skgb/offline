// $Id$


import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import javax.imageio.ImageIO;


class Preferences {
	
	static final String bundleIdentifier = "de.skgb.offline";
	
	File file, newFile;
	Properties prefs;
	
	Preferences () {
		/* We're really searching for two different locations at the same time:
		 * - The first location in search order that we deem to be usable for
		 *   writing TO it (if necessary) [newFile], and
		 * - the first location in search order that actually contains a prefs
		 *   file we can read FROM [file].
		 * If [file] is not found, it remains at null. Default values are
		 * used. The [newFile] will only be used if save() is called while
		 * [file] == null.
		 */
		
		// http://en.wikipedia.org/wiki/Environment_variable#Default_Values_on_Microsoft_Windows
		// http://programmers.stackexchange.com/questions/3956/best-way-to-save-application-settings
		// http://bugs.java.com/bugdatabase/view%5Fbug.do?bug%5Fid=4787931
		
		// OS X: ~/Library/Preferences/
		// Unix: ~/.config/
		
//		System.out.println(new File(String.valueOf( System.getenv("APPDATA") )));  // nicht Mac
//		System.out.println(new File(String.valueOf( System.getenv("USERPROFILE") )));  // nicht Mac, but definitely usable for home dir in Win
//		System.out.println(new File(String.valueOf( System.getenv("HOMEDRIVE") + System.getenv("HOMEPATH") )));  // nicht Mac
//		System.out.println(new File(String.valueOf( System.getenv("LOCALAPPDATA") )));  // nicht Mac + Win XP
//		System.out.println(new File(String.valueOf( System.getProperty("user.home") )));  // discouraged on Win (unreliable)
//		System.out.println(new File(String.valueOf( System.getenv("HOME") )));  // nicht Win
//		System.out.println(new File(String.valueOf( System.getProperty("os.name") )));  // "Windows XP" / "Mac OS X"
//		System.out.println(new File(String.valueOf( System.getProperty("os.arch") )));
//		System.out.println(new File(String.valueOf( System.getProperty("os.version") )));
//		System.out.println(new File("").getAbsoluteFile());
//		System.out.println(new File(String.valueOf( System.getProperty("user.dir") )));
		
		// wie Pfad erzeugen, wenn er nicht existiert? oder einfach überspringen und zur nächsten option? im normalfall müsste das ja alles existieren
		
		String s = System.getProperty("file.separator");
		String prefsPath = System.getProperty("user.home") + s + "Library" + s + "Preferences" + s + bundleIdentifier + ".properties";
		file = new File(prefsPath);
		prefs = new Properties();
	}
	
	Preferences set (String key, Object value) {
		prefs.setProperty(key, String.valueOf(value));
		return this;  // enable chaining
	}
	
	String get (String key) {
		return prefs.getProperty(key);
	}
	
	Preferences load () {
		try {
			FileInputStream stream = new FileInputStream(file);
			Properties properties = new Properties();
			properties.load(stream);
			stream.close();
			if (! bundleIdentifier.equals(properties.getProperty("CFBundleIdentifier"))) {
				System.out.println("CFBundleIdentifier mismatch in prefs file " + file.toString());
				return this;
			}
			prefs = properties;
		}
		catch (FileNotFoundException e) {
			// ignore; normal condition
		}
		catch (IOException e) {
			e.printStackTrace();  // preferences are a non-critical function, so we just log the error
		}
		return this;  // enable chaining
	}
	
	void save () {
		set("CFBundleIdentifier", bundleIdentifier);
		try {
			FileOutputStream stream = new FileOutputStream(file);
			prefs.store(stream, "comment");
			stream.close();
		}
		catch (IOException e) {
			e.printStackTrace();  // preferences are a non-critical function, so we just log the error
		}
	}
	
}

public class Gui implements ActionListener {
	
	static final String version = "0.1";
	
	SkgbOffline app;
	
	Preferences prefs;
	
	GuiWindow window;
	
	Gui () {
		window = new GuiWindow(this);
		window.versionLabel.setText("Version " + version);
//		EventQueue.invokeLater( window );
		window.run();
		
		prefs = new Preferences().load();
		String path = prefs.get("MandateStore");
		if (path != null) {
			File file = new File(path);
			if (file != null && file.isFile()) {
				loadMandateStore(file);
			}
		}
	}
	
	void loadMandateStore (File file) {
		try {
			app = new SkgbOffline(file);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		window.mandateStoreField.setText(file.toString());
		window.debitFileButton.setEnabled(! app.mandateStore.hashMissing);
		window.updateInfoPanel(app.mandateStore);
		
//		System.out.println(app.mandateStore.hashMissing);
//		System.out.println(app.mandateStore.hashMatches);
//		System.out.println(app.mandateStore.hashBase64);
//		System.out.println(app.mandateStore.updated);
	}
	
	public void savePrefs () {
		prefs.set("MandateStore", app.mandateFile.toString());
		prefs.save();
	}
	
	public void actionPerformed (ActionEvent event) {
		if (event.getSource() == window.mandateStoreButton) {
			File file = new CsvFileDialog(window).open("Mandatssammlung öffnen");
			if (file == null) {
				return;
			}
			loadMandateStore(file);
			savePrefs();
			
		}
		else if (event.getSource() == window.debitFileButton) {
			if (app == null) {
				throw new IllegalStateException();
			}
			
			File inFile = new CsvFileDialog(window).open("Lastschriftdatei öffnen");
			if (inFile == null) {
				return;
			}
			
			File outFile = new CsvFileDialog(window).save("Lastschriftdatei mit Kontodaten sichern", "out.csv");
			if (outFile == null) {
				return;
			}
			
			try {
				app.process(inFile, outFile);
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}
			
//			System.out.println(outFile);
//			throw new UnsupportedOperationException();
			
		}
		else {
			throw new UnsupportedOperationException();
		}
	}
	
	public static void main (final String[] args) {
		new Gui();
	}
	
}


class CsvFileDialog {
	
	class Filter implements FilenameFilter {
		public boolean accept (File dir, String name) {
			return name.length() > 4 && name.substring(name.length() - 4).equalsIgnoreCase(".csv");
		}
	}
	
	Frame parent;
	
	CsvFileDialog (Frame parent) {
		this.parent = parent;
	}
	
	File open (String title) {
		FileDialog dialog = new FileDialog(parent, title, FileDialog.LOAD);
		dialog.setFilenameFilter(new Filter());
		dialog.setVisible(true);
		if (dialog.getFile() == null || dialog.getDirectory() == null) {
			return null;
		}
		return new File(dialog.getDirectory() + dialog.getFile());
	}
	
	File save (String title, String suggestedName) {
		FileDialog dialog = new FileDialog(parent, title, FileDialog.SAVE);
		dialog.setFile(suggestedName);  // side effect if this has an extension (e. g. "a.csv"): enables button "hide extension"
		dialog.setVisible(true);
		if (dialog.getFile() == null || dialog.getDirectory() == null) {
			return null;
		}
		return new File(dialog.getDirectory() + dialog.getFile());
	}
	
}


class GuiWindow extends Frame implements Runnable {
	
//	static int titlebarHeight = 22;  // Mac OS X 10.6
//	static int titlebarHeight = 29;  // Windows XP
	static int titlebarHeight = 23;
	
	static Dimension windowSize = new Dimension(557, 456 + titlebarHeight);
	
	static Rectangle fromNSRect (int x, int y, int w, int h) {
		return new Rectangle(x, windowSize.height - y, w, h);
	}
	
	InfoPanel infoPanel;
	TextField mandateStoreField;
	Button mandateStoreButton;
	Button debitFileButton;
	Label versionLabel;
	
	GuiWindow (ActionListener listener) {
		super("SKGB-offline");
		setLayout(null);
		setLocation(300, 100);
		setSize(windowSize);
		setResizable(false);
//		setIconImage();
		
		addWindowListener(new WindowAdapter() {
			public void windowClosing (WindowEvent event) {
				dispose();
				System.exit(0); // expedite (debug)
			}
		});
		
		addMasthead();
		addExplanations();
		
		addLabel(17, 346, 128, 17, "Mandatssammlung:");
		mandateStoreField = new TextField();
		mandateStoreField.setBounds(fromNSRect(150, 348, 382, 22));
		mandateStoreField.setEditable(false);
		add(mandateStoreField);
		mandateStoreButton = new Button("Mandatssammlung öffnen");
		mandateStoreButton.setBounds(fromNSRect(150 -1, 322 +1, 192 +2, 28));
		mandateStoreButton.addActionListener(listener);
		add(mandateStoreButton);
		
		infoPanel = null;
		updateInfoPanel(null);
		
		debitFileButton = new Button("Lastschriftdatei ergänzen");
		debitFileButton.setBounds(fromNSRect(20 -1, 80 +1, 188 +2, 28));
		debitFileButton.setEnabled(false);
		debitFileButton.addActionListener(listener);
		add(debitFileButton);
		
		try {
			final URL url = ClassLoader.getSystemResource("winicon.png");
			if (url == null) {
				new RuntimeException().printStackTrace();
				return;  // non-critical resource; ignore
			}
			setIconImage(ImageIO.read(url));
		}
		catch (Exception exception) {
			exception.printStackTrace();
			// non-critical resource; ignore
		}
	}
	
	void updateInfoPanel (MandateStore mandateStore) {
		if (infoPanel != null) {
			remove(infoPanel);
		}
		infoPanel = new InfoPanel(mandateStore);
		add(infoPanel);
	}
	
	void addMasthead () {
		Font mastheadFont = new Font("Arial", Font.BOLD, 26);
		Label blackLabel = addLabel(17, 415, 80, 31, "SKGB");
		blackLabel.setFont(mastheadFont);
		Label redLabel = addLabel(92, 415, 91, 31, "-offline");
		redLabel.setFont(mastheadFont);
		redLabel.setForeground(Color.RED);
		
		ImageView image = new ImageView("logo.gif");
		image.setBounds(fromNSRect(443, 436, 92, 55));
		add(image);
		
		versionLabel = addLabel(17, 439, 423, 15, "");
		versionLabel.setFont(new Font("Arial", Font.PLAIN, 11));
		versionLabel.setForeground(Color.GRAY);
		versionLabel.setAlignment(Label.RIGHT);
	}
	
	void addExplanations () {
		Font explanationFont = new Font(Font.DIALOG, Font.PLAIN, 11);
		
		addLabel(147, 290, 393, 14, "Die Mandatssammlung ist ein aus Datenschutzgründen nur offline").setFont(explanationFont);
		addLabel(147, 276, 393, 14, "existierendes Dokument, mit dessen Hilfe Lastschriftdateien anhand der").setFont(explanationFont);
		addLabel(147, 262, 393, 14, "Mandatsreferenzen mit den Angaben zur Kontoverbindung ergänzt").setFont(explanationFont);
		addLabel(147, 248, 393, 14, "werden können.").setFont(explanationFont);
		
		addLabel(147, 226, 393, 14, "Die SKGB-Geschäftsführung stellt die Mandatssammlung auf Anfrage").setFont(explanationFont);
		addLabel(147, 212, 393, 14, "zur Verfügung. Du bist selbst dafür verantwortlich, die Sammlung bei").setFont(explanationFont);
		addLabel(147, 198, 393, 14, "Dir vor unbefugtem Zugriff zu schützen! (Verschlüsselung etc.)").setFont(explanationFont);
		
		addLabel(17, 48, 523, 14, "Lastschriftdateien werden in den SEPA-Diensten von SKGB-intern generiert in einem Format, das").setFont(explanationFont);
		addLabel(17, 34, 523, 14, "die Verwendung mit »WISO Mein Geld« erlaubt.").setFont(explanationFont);
	}
	
	Label addLabel (int x, int y, int w, int h, String text) {
		Label label = new Label(text);
		label.setBounds(fromNSRect(x, y, w, h));
		add(label);
		return label;  // enable chaining
	}
	
	@Override
	public void paint (final Graphics g) {
		super.paint(g);
		Rectangle line = fromNSRect(20, 384 -2, 417, 1);
		g.setColor(Color.GRAY);
		g.drawLine(line.x, line.y, line.x + line.width - 1, line.y + line.height - 1);
	}
	
	public void run () {
		setVisible(true);
	}
	
}


class InfoPanel extends Panel {
	
	Label[] label = new Label[3];
	
	InfoPanel (MandateStore mandateStore) {
		super(null);
		setBounds(GuiWindow.fromNSRect(17, 157, 523, 51));
		
		if (mandateStore == null) {
			label[0] = new Label("Derzeit ist keine Mandatssammlung geladen.");
			label[1] = new Label();
			label[2] = new Label();
		}
		else if (mandateStore.hashMissing) {
			label[0] = new Label("Die geöffnete Datei ist keine Mandatssammlung.");
			label[1] = new Label("[Prüfsumme fehlt]");
			label[2] = new Label();
		}
/*		else if (mandateStore.hashMissing) {
			label[0] = new Label("Die geladene Mandatssammlung enthält keine Prüfsumme. Dies kann auf");
			label[1] = new Label("manipulierte Daten hindeuten. Diese Sammlung sollte nicht verwendet werden!");
			label[2] = new Label();
		}
*/		else if (! mandateStore.hashMatches && mandateStore.updated == null) {
			label[0] = new Label("Die geladene Mandatssammlung ist ungültig. Dies kann auf einen Softwarefehler");
			label[1] = new Label("seitens der SKGB-Geschäftsführung hindeuten, aber auch auf korrupte Daten.");
			label[2] = new Label("Diese Sammlung sollte nicht verwendet werden!");
		}
		else if (mandateStore.hashMatches && mandateStore.updated == null) {
			label[0] = new Label("Die geladene Mandatssammlung ist zwar an für sich vollständig und gültig, aber");
			label[1] = new Label("sie ist nicht datiert. Dies kann auf einen Softwarefehler seitens der SKGB-");
			label[2] = new Label("Geschäftsführung hindeuten. Bitte verwende diese Sammlung mit Vorsicht!");
		}
		else if (! mandateStore.hashMatches && mandateStore.updated != null) {
			label[0] = new Label("Die geladene Mandatssammlung wurde mit dem Stand " + mandateStore.updated + " erzeugt, aber");
			label[1] = new Label("danach von Dritten verändert. Bitte verwende diese Sammlung mit Vorsicht!");
			label[2] = new Label();
		}
		else if (mandateStore.hashMatches && mandateStore.updated != null) {
			label[0] = new Label("Die geladene Mandatssammlung ist vollständig und gültig.");
			label[1] = new Label("Stand: " + mandateStore.updated);
			label[2] = new Label();
		}
		else {
			throw new AssertionError();
		}
		
		label[0].setBounds(0, 0, 523, 17);
		label[1].setBounds(0, 17, 523, 17);
		label[2].setBounds(0, 34, 523, 17);
		
		add(label[0]);
		add(label[1]);
		add(label[2]);
	}
	
}
