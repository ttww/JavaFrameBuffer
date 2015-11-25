package org.tw.pi.framebuffer;

import org.junit.Assert;
import org.junit.Test;

public class FrameBufferImageTest {
	
	@Test
	public void testCompatibility16bpp() {
		Assert.assertTrue(FrameBuffers.createColorModel(16).isCompatibleSampleModel(FrameBuffers.createSampleModel(16, 1, 1)));
		Assert.assertTrue(FrameBuffers.createColorModel(16).isCompatibleRaster(new FrameBufferRaster(new FrameBuffer(1,1))));
	}

	@Test
	public void testCompatibility24bpp() {
		Assert.assertTrue(FrameBuffers.createColorModel(24).isCompatibleSampleModel(FrameBuffers.createSampleModel(24, 1, 1)));
		Assert.assertTrue(FrameBuffers.createColorModel(24).isCompatibleRaster(new FrameBufferRaster(new FrameBuffer(1,1))));
	}
}
