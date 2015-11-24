package org.tw.pi.framebuffer;

import java.awt.Point;
import java.awt.image.DataBuffer;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;

public class FrameBufferRaster extends WritableRaster {

	static SampleModel createSampleModel(int w, int h) {
		return new SinglePixelPackedSampleModel(
				DataBuffer.TYPE_INT, 
				w, 
				h, 
				new int[] {
						0x00ff0000,
						0x0000ff00,
						0x000000ff,
				});
	}

	public FrameBufferRaster(String fbdev) {
		this(new FrameBufferDataBuffer(fbdev));
	}
	
	public FrameBufferRaster(FrameBufferDataBuffer dataBuffer) {
		super(createSampleModel(dataBuffer.getWidth(), dataBuffer.getHeight()), dataBuffer, new Point(0, 0));
	}
	
	public void close() {
		((FrameBufferDataBuffer) dataBuffer).close();
	}

}
