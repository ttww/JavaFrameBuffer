package org.tw.pi.framebuffer;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.Closeable;

import org.tw.pi.framebuffer.FrameBuffers.ColorEndian;

public class FrameBufferedImage extends BufferedImage implements Closeable {
	private FrameBuffer fb;
	
	public FrameBufferedImage(String fbdev) {
		this(new FrameBuffer(fbdev));
	}
	
	public FrameBufferedImage(ColorEndian ce, String fbdev) {
		this(ce, new FrameBuffer(fbdev));
	}
	
	public FrameBufferedImage(FrameBuffer fb) {
		this(ColorEndian.RGB, fb);
	}
	
	public FrameBufferedImage(ColorEndian ce, FrameBuffer fb) {
		this(
				new FrameBufferedRaster(
						fb, 
						ce.createSampleModel(fb.getWidth(), fb.getHeight(), fb.getColorDepth())), 
				ce.createColorModel(fb.getColorDepth()));
	}
	
	public FrameBufferedImage(FrameBufferedRaster raster, ColorModel colorModel) {
		super(colorModel, raster, true, null);
		fb = raster.getFrameBuffer();
	}
	
	public void close() {
		fb.close();
	}
	
	public FrameBuffer getFrameBuffer() {
		return fb;
	}
}
