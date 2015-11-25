package org.tw.pi.framebuffer;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;

import org.tw.pi.framebuffer.FrameBuffers.ColorEndian;

public class FrameBufferImage extends BufferedImage {
	private FrameBuffer fb;
	
	public FrameBufferImage(String fbdev) {
		this(new FrameBuffer(fbdev));
	}
	
	public FrameBufferImage(ColorEndian ce, String fbdev) {
		this(ce, new FrameBuffer(fbdev));
	}
	
	public FrameBufferImage(FrameBuffer fb) {
		this(ColorEndian.RGB, fb);
	}
	
	public FrameBufferImage(ColorEndian ce, FrameBuffer fb) {
		this(
				new FrameBufferRaster(
						fb, 
						ce.createSampleModel(fb.getWidth(), fb.getHeight(), fb.getColorDepth())), 
				ce.createColorModel(fb.getColorDepth()));
	}
	
	public FrameBufferImage(FrameBufferRaster raster, ColorModel colorModel) {
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
