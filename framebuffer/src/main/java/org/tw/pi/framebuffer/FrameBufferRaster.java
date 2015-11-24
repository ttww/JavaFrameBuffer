package org.tw.pi.framebuffer;

import java.awt.Point;
import java.awt.image.DataBuffer;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;

public class FrameBufferRaster extends WritableRaster {

	private static SampleModel createSampleModel(FrameBufferDataBuffer dataBuffer) {
		return new SinglePixelPackedSampleModel(
				DataBuffer.TYPE_INT, 
				dataBuffer.getWidth(), 
				dataBuffer.getHeight(), 
				new int[] {
						0x00ffffff
				});
	}

	public FrameBufferRaster(String fbdev) {
		this(new FrameBufferDataBuffer(fbdev));
	}
	
	public FrameBufferRaster(FrameBufferDataBuffer dataBuffer) {
		super(createSampleModel(dataBuffer), dataBuffer, new Point(0, 0));
	}
	
	public void close() {
		((FrameBufferDataBuffer) dataBuffer).close();
	}

}
