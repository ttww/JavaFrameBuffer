package org.tw.pi.framebuffer;

import org.junit.Assert;
import org.junit.Test;
import org.tw.pi.framebuffer.FrameBuffers.ColorEndian;

public class FrameBufferImageTest {
	
	@Test
	public void testCompatibility16bpp() {
		ColorEndian ce = ColorEndian.RGB;
		Assert.assertTrue(FrameBuffers.createColorModel(ce, 16).isCompatibleSampleModel(FrameBuffers.createSampleModel(ce, 1, 1, 16)));
		Assert.assertTrue(FrameBuffers.createColorModel(ce, 16).isCompatibleRaster(new FrameBufferRaster(ce, new FrameBuffer(1, 1, 16))));
		ce = ColorEndian.BGR;
		Assert.assertTrue(FrameBuffers.createColorModel(ce, 16).isCompatibleSampleModel(FrameBuffers.createSampleModel(ce, 1, 1, 16)));
		Assert.assertTrue(FrameBuffers.createColorModel(ce, 16).isCompatibleRaster(new FrameBufferRaster(ce, new FrameBuffer(1, 1, 16))));
	}

	@Test
	public void testCompatibility24bpp() {
		ColorEndian ce = ColorEndian.RGB;
		Assert.assertTrue(FrameBuffers.createColorModel(ce, 24).isCompatibleSampleModel(FrameBuffers.createSampleModel(ce, 1, 1, 24)));
		Assert.assertTrue(FrameBuffers.createColorModel(ce, 24).isCompatibleRaster(new FrameBufferRaster(ce, new FrameBuffer(1, 1, 24))));
		ce = ColorEndian.BGR;
		Assert.assertTrue(FrameBuffers.createColorModel(ce, 24).isCompatibleSampleModel(FrameBuffers.createSampleModel(ce, 1, 1, 24)));
		Assert.assertTrue(FrameBuffers.createColorModel(ce, 24).isCompatibleRaster(new FrameBufferRaster(ce, new FrameBuffer(1, 1, 24))));
	}
}
