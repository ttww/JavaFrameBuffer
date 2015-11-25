package org.tw.pi.framebuffer;

import java.awt.Point;
import java.awt.image.WritableRaster;

public class FrameBufferRaster extends WritableRaster {

	public FrameBufferRaster(FrameBufferDataBuffer fb) {
		super(FrameBuffer.createSampleModel(fb.getColorDepth(), fb.getWidth(), fb.getHeight()), fb, new Point(0, 0));
	}

}
