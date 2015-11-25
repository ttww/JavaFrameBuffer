package org.tw.pi.framebuffer;

import java.awt.image.BufferedImage;

public class FrameBufferImage extends BufferedImage {
	private FrameBuffer fb;
	
	public FrameBufferImage(String fbdev) {
		this(new FrameBuffer(fbdev));
	}
	
	public FrameBufferImage(FrameBuffer fb) {
		super(FrameBuffers.createColorModel(fb.getColorDepth()), new FrameBufferRaster(fb), true, null);
		this.fb = fb;
	}
	
	public void close() {
		fb.close();
	}
	
	public FrameBuffer getFrameBuffer() {
		return fb;
	}
}
