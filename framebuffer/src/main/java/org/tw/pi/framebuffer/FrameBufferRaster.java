package org.tw.pi.framebuffer;

import java.awt.Point;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;

import org.tw.pi.framebuffer.FrameBuffers.ColorEndian;

public class FrameBufferRaster extends WritableRaster {

	private FrameBuffer fb;
	
	public FrameBufferRaster(FrameBuffer fb) {
		this(ColorEndian.RGB, fb);
	}
	
	public FrameBufferRaster(ColorEndian ce, FrameBuffer fb) {
		this(fb, ce.createSampleModel(fb.getWidth(), fb.getHeight(), fb.getColorDepth()));
	}

	public FrameBufferRaster(FrameBuffer fb, SampleModel sampleModel) {
		super(sampleModel, fb, new Point(0,0));
		this.fb = fb;
	}
	
	public FrameBuffer getFrameBuffer() {
		return fb;
	}
}
