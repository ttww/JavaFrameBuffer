package org.tw.pi.framebuffer;

import org.junit.Assert;
import org.junit.Test;

public class FrameBufferImageTest {
	
	@Test
	public void testCompatibility16bpp() {
		Assert.assertTrue(FrameBuffers.createColorModelRGB(16).isCompatibleSampleModel(FrameBuffers.createSampleModelRGB(1, 1, 16)));
		Assert.assertTrue(FrameBuffers.createColorModelRGB(16).isCompatibleRaster(new FrameBufferRaster(new FrameBuffer(1, 1, 16))));
	}

	@Test
	public void testCompatibility24bpp() {
		Assert.assertTrue(FrameBuffers.createColorModelRGB(24).isCompatibleSampleModel(FrameBuffers.createSampleModelRGB(1, 1, 24)));
		Assert.assertTrue(FrameBuffers.createColorModelRGB(24).isCompatibleRaster(new FrameBufferRaster(new FrameBuffer(1, 1, 24))));
	}
}
