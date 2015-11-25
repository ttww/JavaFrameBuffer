package org.tw.pi.framebuffer;

import java.awt.Point;
import java.awt.image.WritableRaster;

public class FrameBufferRaster extends WritableRaster {

	public FrameBufferRaster(FrameBuffer fb) {
		super(FrameBuffers.createSampleModel(fb.getColorDepth(), fb.getWidth(), fb.getHeight()), fb, new Point(0, 0));
	}

}
