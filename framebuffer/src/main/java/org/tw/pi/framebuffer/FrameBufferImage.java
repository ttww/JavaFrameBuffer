package org.tw.pi.framebuffer;

import java.awt.image.BufferedImage;

public class FrameBufferImage extends BufferedImage {
	private FrameBufferDataBuffer fb;
	
	public FrameBufferImage(String fbdev) {
		this(new FrameBufferDataBuffer(fbdev));
	}
	
	public FrameBufferImage(FrameBufferDataBuffer fb) {
		super(FrameBuffer.createColorModel(fb.getColorDepth()), new FrameBufferRaster(fb), true, null);
		this.fb = fb;
	}
	
	public void close() {
		fb.close();
	}
	
	public FrameBufferDataBuffer getFrameBuffer() {
		return fb;
	}
}
