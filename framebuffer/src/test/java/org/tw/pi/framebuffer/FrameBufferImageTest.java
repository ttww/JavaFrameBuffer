package org.tw.pi.framebuffer;

import org.junit.Assert;
import org.junit.Test;

public class FrameBufferImageTest {
	
	@Test
	public void testCompatibility16bpp() {
		Assert.assertTrue(FrameBuffer.createColorModel(16).isCompatibleSampleModel(FrameBuffer.createSampleModel(16, 1, 1)));
		Assert.assertTrue(FrameBuffer.createColorModel(16).isCompatibleRaster(new FrameBufferRaster(new FrameBufferDataBuffer(1,1))));
	}

	@Test
	public void testCompatibility24bpp() {
		Assert.assertTrue(FrameBuffer.createColorModel(24).isCompatibleSampleModel(FrameBuffer.createSampleModel(24, 1, 1)));
		Assert.assertTrue(FrameBuffer.createColorModel(24).isCompatibleRaster(new FrameBufferRaster(new FrameBufferDataBuffer(1,1))));
	}
}
