package org.tw.pi.framebuffer;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.Closeable;
import java.io.IOException;

/**
 * {@link BufferedImage} whose data is backed by a {@link FrameBuffer}, itself
 * either a dummy or backed by a linux framebuffer device.
 * @author Robin Kirkman
 *
 */
public class FrameBufferedImage extends BufferedImage implements Closeable {
	/**
	 * The {@link FrameBuffer} backing this {@link FrameBufferedImage}
	 */
	final private FrameBuffer fb;
	
	/**
	 * Create a new {@link FrameBufferedImage} by opening a linux framebuffer device
	 * @param fbdev The path to the framebuffer, such as {@code "/dev/fb1"}
	 * @throws IOException If the framebuffer could not be opened
	 */
	public FrameBufferedImage(String fbdev) throws IOException {
		this(new FrameBuffer(fbdev));
	}
	
	/**
	 * Create a new {@link FrameBufferedImage} by opening a linux framebuffer device
	 * with a specified color endian-ness
	 * @param ce The color endian-ness: {@link ColorEndian#RGB} or {@link ColorEndian#BGR}
	 * @param fbdev The path to the framebuffer, such as {@code "/dev/fb1"}
	 * @throws IOException If the framebuffer could not be opened
	 */
	public FrameBufferedImage(ColorEndian ce, String fbdev) throws IOException {
		this(ce, new FrameBuffer(fbdev));
	}
	
	/**
	 * Create a new {@link FrameBufferedImage} by wrapping an existing {@link FrameBuffer}
	 * @param fb The {@link FrameBuffer} to wrap
	 */
	public FrameBufferedImage(FrameBuffer fb) {
		this(ColorEndian.RGB, fb);
	}
	
	/**
	 * Create a new {@link FrameBufferedImage} by wrapping an existing {@link FrameBuffer} with a
	 * specified color endian-ness
	 * @param ce The color endian-ness: {@link ColorEndian#RGB} or {@link ColorEndian#BGR}
	 * @param fb The {@link FrameBuffer} to wrap
	 */
	public FrameBufferedImage(ColorEndian ce, FrameBuffer fb) {
		this(
				new FrameBufferedRaster(ce, fb), 
				ce.createColorModel(fb.getColorDepth()));
	}
	
	/**
	 * Create a new {@link FrameBufferedImage} wrapping a {@link FrameBufferedRaster}
	 * and a {@link ColorModel} returned by {@link ColorEndian#createColorModel(int)}
	 * @param raster The {@link FrameBufferedRaster} to wrap
	 * @param colorModel The {@link ColorModel} to use
	 */
	private FrameBufferedImage(FrameBufferedRaster raster, ColorModel colorModel) {
		super(colorModel, raster, true, null);
		fb = raster.getFrameBuffer();
	}
	
	public void close() {
		fb.close();
	}
	
	/**
	 * Returns the {@link FrameBuffer} backing this {@link FrameBufferedImage}
	 * @return
	 */
	public FrameBuffer getFrameBuffer() {
		return fb;
	}
}
