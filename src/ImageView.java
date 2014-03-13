// $Id$


import java.awt.Dimension;
import java.awt.Image;
import java.awt.Graphics;
import java.awt.Panel;
import java.net.URL;
import javax.imageio.ImageIO;


public class ImageView extends Panel { 
	
	Image image;
	
	ImageView (final Image image) {
		this.image = image;
	}
	
	ImageView (final String resourceName) {
		try {
			final URL url = ClassLoader.getSystemResource(resourceName);
			if (url == null) {
				throw new IllegalArgumentException("Image '" + resourceName + "' is not present in application package.");
			}
			image = ImageIO.read(url);
		}
		catch (Exception exception) {
			exception.printStackTrace();
		}
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
