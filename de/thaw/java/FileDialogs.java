// $Id$

package de.thaw.java;


import java.awt.Frame;
import java.awt.FileDialog;
import java.io.File;
import java.io.FilenameFilter;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;


/**
 * Standard OS open and save dialogs. This class is motivated by the combination
 * of Swing's abysmal file chooser dialogs in the Mac OS X look-and-feel and the
 * less-than-great Windows native AWT dialogs.
 * <p>
 * The general idea is that for OS X the excellent Mac native dialogs are used,
 * while Windows's own broken native dialogs are transparently replaced by
 * Swing's. This is a rough implementation with little regard to other OSs
 * (Linux?). I don't use Windows if I can at all help it, so I'm sure the
 * Windows-only part of this class could use some love. At any rate, this is
 * a really big improvement over cross-platform AWT-only or Swing-only file
 * chooser dialogs.
 */
public class FileDialogs {
	
	
	/**
	 * Whether native AWT dialogs should be used in favour of "light-weight"
	 * Swing dialogs. The value of this field is determined by analysing the
	 * <code>os.name</code> system property; Swing dialogs are only chosen on
	 * Windows.
	 */
	static final boolean nativeDialogs;
	static {
		boolean win = false;
		try {
			win = String.valueOf( System.getProperty("os.name") ).contains("Windows");
		}
		catch (SecurityException e) {
			// always default to use the native dialogs
		}
		nativeDialogs = ! win;
	}
	
	
	/**
	 * Checks whether the given file name ends with the specified extension.
	 * @param filename .
	 * @param extension .
	 * @return true if the file name does end with that extension or if the
	 *  extension is <code>null</code>
	 * @throws NullPointerException if <code>filename == null</code>
	 */
	static boolean hasExtension (final String filename, final String extension) {
		if (extension == null) {
			return true;
		}
		final int lengthProper = filename.length() - extension.length();
		return lengthProper > 0 && filename.substring(lengthProper).equalsIgnoreCase( extension );
	}
	
	
	/**
	 * A file name filter adapter that implements both the AWT and the Swing
	 * interface for such filters at the same time. Provides a default
	 * implementation for convenience to filter files with a specific extension.
	 */
	public static class Filter extends FileFilter implements FilenameFilter {
		
		/** This filter's extension. */
		final String extension;
		
		/**
		 * @param extension the file name extension to filter files on
		 */
		public Filter (final String extension) {
			this.extension = extension;
		}
		
		/**
		 * Adapter for AWT.
		 * @param dir {@inheritDoc}
		 * @param name {@inheritDoc}
		 * @return {@inheritDoc}
		 */
		public boolean accept (final File dir, final String name) {
			return accept(String.valueOf(name));
		}
		
		/**
		 * Adapter for Swing.
		 * @param file {@inheritDoc}
		 * @return {@inheritDoc}
		 */
		public boolean accept (final File file) {
			if (file.isDirectory()) {
				return true;
				// the file system may not be navigable if this returns false
				// that dirs are not selectable with Save is something this filter's client needs to handle (which it does by means of setFileSelectionMode's default FILES_ONLY)
			}
			return accept(String.valueOf(file));
		}
		
		/**
		 * Tests if the specified file name should be included in a file list.
		 * This default implementation simply checks the file name for this
		 * filter's file name extension. Override this method to implement your
		 * own filter logic.
		 * @param filename .
		 * @return true iff the name ends with the extension provided to the
		 *  constructor; false otherwise.
		 */
		public boolean accept (final String filename) {
			return hasExtension(String.valueOf(filename), extension);
		}
		
		/**
		 * The description of this filter. Override this method to give your
		 * own description.
		 * @return this filter's extension, prepended by an asterisk.
		 */
		public String getDescription () {
			return "*" + extension;
		}
		
	}
	
	
	class SaveFileChooser extends JFileChooser {
		
		class ReplaceFileDialog extends JOptionPane {
			
			String replaceCaption = "Ersetzen";
			String cancelCaption = "Abbrechen";
			
			ReplaceFileDialog (File file) {
				super(new String[]{
					"<html><b>»" + file.getName() + "« existiert bereits.<br>Möchten Sie das Objekt ersetzen?</b></html>",
					"<html><small>Es existiert bereits eine Datei oder ein Ordner mit<br>demselben Namen " +
					(file.getParentFile().getName().length() > 0 ? "im Ordner " + file.getParentFile().getName() : "in diesem Ordner") +
					".<br>Beim Ersetzen wird der Inhalt überschrieben.</small></html>"
				});
				setOptions(new String[]{ replaceCaption, cancelCaption });
				setInitialValue( getOptions()[1] );
				setMessageType(JOptionPane.WARNING_MESSAGE);
			}
			
			boolean shouldReplace () {
				JDialog dialog = createDialog(SaveFileChooser.this, title);
				dialog.setVisible(true);
				dialog.dispose();
				return replaceCaption.equals(getValue());
			}
			
		}
		
		Frame parent;
		Filter filter;
		String title;
		
		SaveFileChooser (Frame parent, String title, Filter filter) {
			super(lastOpenPath);
			this.parent = parent;
			this.title = title;
			this.filter = filter;
			if (filter != null) {
				addChoosableFileFilter(filter);
				setFileFilter(filter);
			}
			setDialogType(JFileChooser.SAVE_DIALOG);
			setDialogTitle(title);
		}
		
		@Override
		public void approveSelection () {
			File file = getSelectedFile();
			if (file == null) {
				return;
			}
			String name = file.getName();
			
			// fix extension
			if (getFileFilter() == filter && filter != null && ! hasExtension(name, filter.extension)) {
				name += filter.extension;
				file = new File(file.getParentFile(), name);
				setSelectedFile(file);
			}
			
			if (file.exists() && ! new ReplaceFileDialog(file).shouldReplace() ) {
				return;  // we don't approve
			}
			super.approveSelection();  // we do approve
		}
		
		File show (String suggestedName) {
			setSelectedFile(new File(suggestedName));
			int result = showDialog(parent, null);;
			if (result != JFileChooser.APPROVE_OPTION) {
				return null;
			}
			return getSelectedFile();
		}
		
	}
	
	
	final Frame parent;
	String lastOpenPath = null;
	
	
	public FileDialogs (final Frame parent) {
		this.parent = parent;
	}
	
	
	public File open (final String title, final String path, final Filter filter) {
		// TODO: implement Swing here (just for consistency)
		
		final FileDialog dialog = new FileDialog(parent, title, FileDialog.LOAD);
		if (path != null && path.length() > 0) {
			dialog.setDirectory(path);
		}
		if (filter != null) {
			dialog.setFilenameFilter(filter);
		}
		dialog.setVisible(true);
		if (dialog.getFile() == null || dialog.getDirectory() == null) {
			return null;
		}
		lastOpenPath = dialog.getDirectory();
		return new File(dialog.getDirectory() + dialog.getFile());
	}
	
	
	public File save (final String title, final String suggestedName, final Filter filter) {
		if (! nativeDialogs) {
			final SaveFileChooser saveDialog = new SaveFileChooser(parent, title, filter);
			return saveDialog.show(suggestedName);
			// the Win native AWT dialog isn't too great, see #124
			// OTOH the Mac OS X laf's Swing dialog is abysmal
		}
		
		final FileDialog dialog = new FileDialog(parent, title, FileDialog.SAVE);
		dialog.setFile(suggestedName);  // side effect on OS X if this has an extension (e. g. "a.csv"): enables button "hide extension"
		dialog.setVisible(true);
		if (dialog.getFile() == null || dialog.getDirectory() == null) {
			return null;
		}
		return new File(dialog.getDirectory() + dialog.getFile());
	}
	
}
