package org.tw.pi.framebuffer;

import java.awt.image.DataBuffer;

public class FrameBufferDataBuffer extends DataBuffer {
	
	private static long open(String fbdev) {
		long ptr = FrameBuffer.openDevice(fbdev);

		if (ptr < 10) {
			throw new IllegalArgumentException("Init. for frame buffer " + fbdev + " failed with error code " + ptr);
		}
		
		return ptr;
	}

	private long ptr;
	
	public FrameBufferDataBuffer(String fbdev) {
		this(open(fbdev));
	}
	
	private FrameBufferDataBuffer(long ptr) {
		super(TYPE_INT, FrameBuffer.getDeviceWidth(ptr) * FrameBuffer.getDeviceHeight(ptr));
		this.ptr = ptr;
	}
	
	@Override
	public int getElem(int bank, int i) {
		if(ptr == 0)
			throw new IllegalStateException();
		return FrameBuffer.readRGB(ptr, i);
	}

	@Override
	public void setElem(int bank, int i, int val) {
		if(ptr == 0)
			throw new IllegalStateException();
		FrameBuffer.writeRGB(ptr, i, val);
	}
	
	public void close() {
		if(ptr != 0) {
			FrameBuffer.closeDevice(ptr);
			ptr = 0;
		}
	}

	public int getWidth() {
		if(ptr == 0)
			throw new IllegalStateException();
		return FrameBuffer.getDeviceWidth(ptr);
	}
	
	public int getHeight() {
		if(ptr == 0)
			throw new IllegalStateException();
		return FrameBuffer.getDeviceHeight(ptr);
	}
}
