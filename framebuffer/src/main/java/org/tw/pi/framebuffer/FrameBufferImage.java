package org.tw.pi.framebuffer;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;

import org.tw.pi.framebuffer.FrameBuffers.ColorEndian;

public class FrameBufferImage extends BufferedImage {
	private FrameBuffer fb;
	
	public FrameBufferImage(String fbdev) {
		this(new FrameBuffer(fbdev));
	}
	
	public FrameBufferImage(String fbdev, ColorEndian ce) {
		this(new FrameBuffer(fbdev), ce);
	}
	
	public FrameBufferImage(FrameBuffer fb) {
		this(fb, ColorEndian.RGB);
	}
	
	public FrameBufferImage(FrameBuffer fb, ColorEndian ce) {
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
