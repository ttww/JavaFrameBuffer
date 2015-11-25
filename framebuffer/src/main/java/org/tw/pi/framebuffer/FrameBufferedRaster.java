package org.tw.pi.framebuffer;

import java.awt.Point;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.Closeable;

import org.tw.pi.framebuffer.FrameBuffers.ColorEndian;

public class FrameBufferedRaster extends WritableRaster implements Closeable {

	private FrameBuffer fb;
	
	public FrameBufferedRaster(FrameBuffer fb) {
		this(ColorEndian.RGB, fb);
	}
	
	public FrameBufferedRaster(ColorEndian ce, FrameBuffer fb) {
		this(fb, ce.createSampleModel(fb.getWidth(), fb.getHeight(), fb.getColorDepth()));
	}

	public FrameBufferedRaster(FrameBuffer fb, SampleModel sampleModel) {
		super(sampleModel, fb, new Point(0,0));
		this.fb = fb;
	}
	
	public FrameBuffer getFrameBuffer() {
		return fb;
	}
	
	@Override
	public void close() {
		fb.close();
	}
}
