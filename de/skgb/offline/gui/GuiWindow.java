/* $Id$
 * 
 * Copyright (c) 2014-2016 Arne Johannessen
 * All rights reserved. See LICENSE for details.
 */

package de.skgb.offline.gui;


import de.skgb.offline.MandateStore;

import de.thaw.java.AWTImageView;

import java.awt.Button;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Label;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.MenuShortcut;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.Method;
import java.net.URL;
import javax.imageio.ImageIO;


/**
 * The app's main window, including all widgets and minimal glue code.
 * <p>
 * &lt;rant&gt;
 * <br>
 * This window has been coded using AWT in the hope of getting a native UI on
 * all platforms. However, it turned out that various Windows versions do not
 * in fact use UI widgets with the native look-and-feel. Apparently even modern
 * JRE versions for some reason use heavy-weight components with the
 * look-and-feel of pre-XP Windows, which look decidedly out-of-place in XP,
 * let alone more modern {cough} versions of Windoze. Of course, on OS X AWT
 * does use heavy-weight components with the native look-and-feel, while Swing
 * struggles to make its all-Java light-weight components look native. In the
 * end the only option would seem to be to write a AWT UI for OS X and a Swing
 * UI for Windoze -- which of course is not an option. Except for the menu bar
 * and the app icon and the packaging code, which needs to be different. And
 * except for the preferences and system paths, which are different anyway. And
 * except for the differing UI conventions, e. g. Windoze's totally brain-dead
 * insistence on using right-to-left text in western-language dialog boxes. And
 * except for file chooser dialogs, which are totally unusable in Swing on OS X
 * and slightly-less-totally unusable in AWT on Windoze. Good thing Java is
 * cross-platform! Oh wait... m-(
 * <br>
 * &lt;/rant&gt;
 * <p>
 * Luckily, in the end, this class was just a small experiment of using Xcode /
 * Interface Builder for rapid prototyping of an AWT window in the spirit of
 * the long-defunct Nib4j. Even with the fromNSRect helper method, transferring
 * widget measures from Xcode to Java source code manually turned out to be
 * cumbersome and error-prone. With that and with AWT's inherent failures this
 * experiment is clearly failed.
 * <p>
 * This class works well enough for now, but will require a complete rewrite
 * from scratch using mostly Swing (or possibly JavaFX) as soon as any major
 * changes are required.
 */
class GuiWindow extends Frame implements Runnable {
	
	
	/* The Java UI model is a poor match for any version of Mac OS. To provide
	 * a reasonable user experience on the Mac, there is no alternative to
	 * special-case parts of the UI based on the OS.
	 */
	static final boolean isMac = String.valueOf( System.getProperty("os.name") ).toLowerCase().startsWith("mac");
	
	
	/**
	 * Height of the titlebar. Used for positioning of widgets, as AWT includes
	 * the titlebar in its total dimensions, which makes cross-platform
	 * positioning more difficult.
	 */
	static final int titlebarHeight = isMac ? 22 : 26;
	
	
	/**
	 * Dimensions of this window, taking into account the titlebar's height.
	 */
	static final Dimension windowSize = new Dimension(557, 456 + titlebarHeight);
	
	
	/**
	 * Convert from NSRect to Java AWT coordinates.
	 * @param x from left
	 * @param y from bottom
	 * @param w width
	 * @param h height
	 * @return <code>new Rectangle(x, windowSize.height - y, w, h)</code>
	 * @see <a href=http://cocoadevcentral.com/d/intro_to_quartz/>Introduction to Quartz</a>
	 */
	static Rectangle fromNSRect (final int x, final int y, final int w, final int h) {
		return new Rectangle(x, windowSize.height - y, w, h);
	}
	
	
	/** TextField to make the chosen mandate store's path user-selectable (for copy/paste). */
	final TextField mandateStoreField;
	
	/** Button to select the mandate store. */
	final Button mandateStoreButton;
	
	/** Button to choose the debit file and start the processing. */
	final Button debitFileButton;
	
	/** Area for feedback to the user about the chosen mandate store. */
	InfoPanel infoPanel;
	
	/** Display the GUI version number. */
	Label versionLabel;
	
	/** Close Window menu item (used on OS X). */
	MenuItem closeMenuItem;
	
	
	/**
	 * Set up the window.
	 * @param listener the ActionListener to send button clicks to
	 */
	GuiWindow (final ActionListener listener) {
		super("SKGB-offline");
		setLayout(null);
		setLocation(300, 100);
		setSize(windowSize);
		setResizable(false);
		setAppIcon();
		setMenuBar(listener);
		
		addWindowListener(new WindowAdapter() {
			public void windowClosing (WindowEvent event) {
				listener.actionPerformed(new ActionEvent(event, event.getID(), "close"));
			}
		});
		
		addMasthead();
		addExplanations();
		versionLabel.addMouseListener(new MouseAdapter() {
			public void mouseClicked (MouseEvent event) {
				listener.actionPerformed(new ActionEvent(event, event.getID(), "click"));
			}
		});
		
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
	}
	
	
	/**
	 * Sets this window's menu bar. As this app currently runs with one window
	 * only and closing that window quits the app, there is no need to have a
	 * no-window menu bar for the Mac at this time.
	 * @param listener the ActionListener to send menu item selections to
	 */
	private void setMenuBar (final ActionListener listener) {
		if (isMac) {
			System.setProperty("com.apple.macos.useScreenMenuBar", "true");  // shouldn't be necessary in AWT
			System.setProperty("apple.laf.useScreenMenuBar", "true");  // shouldn't be necessary in AWT
			MenuBar menuBar = new MenuBar();
			Menu fileMenu = new Menu("Ablage");
			closeMenuItem = new MenuItem("Schließen", new MenuShortcut('W'));
			closeMenuItem.addActionListener(listener);
			fileMenu.add(closeMenuItem);
			menuBar.add(fileMenu);
			setMenuBar(menuBar);
		}
		else {
			// Windows: close menu item is provided by the OS; no menu bar necessary
			// other OSs: ?
		}
	}
	
	
	/**
	 * Displays the SKGB logo as application icon.
	 */
	private void setAppIcon () {
		try {
			final URL url = ClassLoader.getSystemResource("winicon.png");
			if (url == null) {
				return;  // non-critical resource; ignore
			}
			Image icon = ImageIO.read(url);
			setIconImage(icon);
			
			/* Because Mac OS uses application icons rather than window icons,
			 * the standard AWT method doesn't do anything. The classes Apple
			 * provided are not available on other systems, so we use
			 * reflection. Unless this isn't a Mac, in which case we bail.
			 */
			if (! isMac) {
				return;
			}
			Class<?> appClass = Class.forName("com.apple.eawt.Application");
			Method setDockIconImage = appClass.getMethod("setDockIconImage", Image.class);
			Object app = appClass.getMethod("getApplication").invoke(null);
			setDockIconImage.invoke(app, icon);
		}
		catch (Exception exception) {
			exception.printStackTrace();
			// non-critical resource; ignore
		}
	}
	
	
	/**
	 * Updates the feedback about the chosen mandate store. The info panel is
	 * simply disposed of and recreated afresh with the new feedback.
	 * @param mandateStore the MandateStore to give feedback to the user about
	 */
	void updateInfoPanel (final MandateStore mandateStore) {
		if (infoPanel != null) {
			remove(infoPanel);
		}
		infoPanel = new InfoPanel(mandateStore);
		add(infoPanel);
	}
	
	
	private void addMasthead () {
		final Font mastheadFont = new Font("Arial", Font.BOLD, 26);
		final Label blackLabel = addLabel(17, 415, 80, 31, "SKGB");
		blackLabel.setFont(mastheadFont);
		final Label redLabel = addLabel(92, 415, 91, 31, "-offline");
		redLabel.setFont(mastheadFont);
		redLabel.setForeground(Color.RED);
		
		final AWTImageView image = new AWTImageView("logo.gif");
		image.setBounds(fromNSRect(443, 436, 92, 55));
		add(image);
		
		versionLabel = addLabel(17, 439, 423, 15, "");
		versionLabel.setFont(new Font("Arial", Font.PLAIN, 11));
		versionLabel.setForeground(Color.GRAY);
		versionLabel.setAlignment(Label.RIGHT);
	}
	
	
	private void addExplanations () {
		final Font explanationFont = new Font(Font.DIALOG, Font.PLAIN, 11);
		
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
	
	
	private Label addLabel (final int x, final int y, final int w, final int h, final String text) {
		final Label label = new Label(text);
		label.setBounds(fromNSRect(x, y, w, h));
		add(label);
		return label;  // enable chaining
	}
	
	
	@Override
	public void paint (final Graphics g) {
		super.paint(g);
		final Rectangle line = fromNSRect(20, 384 -2, 417, 1);
		g.setColor(Color.GRAY);
		g.drawLine(line.x, line.y, line.x + line.width - 1, line.y + line.height - 1);
	}
	
	
	public void run () {
		setVisible(true);
	}
	
	
	/**
	 * <code>Panel</code> for feedback to the user about the chosen mandate store.
	 */
	static class InfoPanel extends Panel {
		
		/** Three separate fixed-length lines of text for feedback. */
		final Label[] label = new Label[3];
		
		/**
		 * Give feedback about the chosen mandate store.
		 * @param mandateStore .
		 */
		InfoPanel (final MandateStore mandateStore) {
			super(null);
			setBounds(GuiWindow.fromNSRect(17, 157, 523, 51));
			
			if (mandateStore == null) {
				label[0] = new Label("Derzeit ist keine Mandatssammlung geladen.");
				label[1] = new Label();
				label[2] = new Label();
			}
			else if (mandateStore.error != null) {
				label[0] = new Label("Die geöffnete Datei ist keine Mandatssammlung.");
				label[1] = new Label("[" + mandateStore.error + "]");
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
