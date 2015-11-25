package org.tw.pi.framebuffer;

import org.junit.Assert;
import org.junit.Test;

public class FrameBufferedImageTest {
	
	@Test
	public void testCompatibility16bpp() {
		FrameBufferedRaster r = null;
		FrameBuffer fb = null;
		ColorEndian ce = ColorEndian.RGB;
		Assert.assertTrue(ce.createColorModel(16).isCompatibleSampleModel(ce.createSampleModel(1, 1, 16)));
		Assert.assertTrue(ce.createColorModel(16).isCompatibleRaster(r = new FrameBufferedRaster(ce, fb = new FrameBuffer(1, 1, 16))));
		r.close();
		fb.close();
		ce = ColorEndian.BGR;
		Assert.assertTrue(ce.createColorModel(16).isCompatibleSampleModel(ce.createSampleModel(1, 1, 16)));
		Assert.assertTrue(ce.createColorModel(16).isCompatibleRaster(r = new FrameBufferedRaster(ce, fb = new FrameBuffer(1, 1, 16))));
		r.close();
		fb.close();
	}

	@Test
	public void testCompatibility24bpp() {
		FrameBufferedRaster r = null;
		FrameBuffer fb = null;
		ColorEndian ce = ColorEndian.RGB;
		Assert.assertTrue(ce.createColorModel(24).isCompatibleSampleModel(ce.createSampleModel(1, 1, 24)));
		Assert.assertTrue(ce.createColorModel(24).isCompatibleRaster(r = new FrameBufferedRaster(ce, fb = new FrameBuffer(1, 1, 24))));
		r.close();
		fb.close();
		ce = ColorEndian.BGR;
		Assert.assertTrue(ce.createColorModel(24).isCompatibleSampleModel(ce.createSampleModel(1, 1, 24)));
		Assert.assertTrue(ce.createColorModel(24).isCompatibleRaster(r = new FrameBufferedRaster(ce, fb = new FrameBuffer(1, 1, 24))));
		r.close();
		fb.close();
	}
}
