// $Id$

package de.skgb.offline.gui;


import de.skgb.offline.MandateStore;

import de.thaw.java.AWTImageView;

import java.awt.Frame;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.Button;
import java.awt.Label;
import java.awt.Graphics;
import java.awt.Font;
import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import javax.imageio.ImageIO;


/**
 * The app's main window, including all widgets and minimal glue code.
 */
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
				new RuntimeException().printStackTrace();  // :DEBUG:
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
		
		AWTImageView image = new AWTImageView("logo.gif");
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
}
