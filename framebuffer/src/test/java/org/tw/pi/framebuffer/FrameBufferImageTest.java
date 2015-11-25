package org.tw.pi.framebuffer;

import org.junit.Assert;
import org.junit.Test;

public class FrameBufferImageTest {
	@Test
	public void testCompatibility() {
		Assert.assertTrue(FrameBufferImage.createColorModel().isCompatibleSampleModel(FrameBufferRaster.createSampleModel(1, 1)));
		Assert.assertTrue(FrameBufferImage.createColorModel().isCompatibleRaster(new FrameBufferRaster(new FrameBufferDataBuffer(-1))));
	}
}
