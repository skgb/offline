// $Id$

package de.thaw.java;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Graphics;
import java.awt.Panel;
import java.net.URL;
import javax.imageio.ImageIO;


/**
 * A Panel that displays an Image. This implementation is mutable in accordance
 * with the general approach of the AWT API. If there is no image to be
 * displayed (i. e. image == null), no exception is thrown -- the instance
 * simply behaves just like your plain vanilla empty Panel would.
 */
public class AWTImageView extends Panel { 
	
	
	/** The image to be displayed by this panel. */
	public Image image;
	
	
	/**
	 * A new instance backed by the specified image.
	 * @param image the Image to be displayed
	 */
	public AWTImageView (final Image image) {
		this.image = image;
	}
	
	
	/**
	 * A new instance backed by the image specified by the given system
	 * resource. There is no exepction if the resource doesn't exist or can't
	 * be loaded -- instead this constructor simply prints a stack trace to
	 * stderr and moves on. This allows clients to dispense with error handling
	 * that would in most cases just do the same anyway.
	 * @param resourceName {@link ClassLoader#getSystemResource(String)} param
	 */
	public AWTImageView (final String resourceName) {
		this( readImage(resourceName) );
	}
	
	
	static Image readImage (final String resourceName) {
		try {
			final URL url = ClassLoader.getSystemResource(resourceName);
			if (url == null) {
				throw new IllegalArgumentException("Image '" + resourceName + "' is not present in application package.");
			}
			return ImageIO.read(url);
		}
		catch (Exception exception) {
			exception.printStackTrace();
		}
		return null;
	}
	
	
	@Override
	public void paint (final Graphics g) {
		super.paint(g);
		if (image != null) {
			g.drawImage(image, 0, 0, image.getWidth(this), image.getHeight(this), this);
		}
	}
	
	
	@Override
	public Dimension getPreferredSize() {
		if (image == null) {
			return super.getPreferredSize();
		}
		return new Dimension(image.getWidth(this), image.getHeight(this));
	}
	
}
