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
	
	FrameBufferDataBuffer(long ptr) {
		super(TYPE_INT, ptr == -1 ? 1 : FrameBuffer.getDeviceWidth(ptr) * FrameBuffer.getDeviceHeight(ptr));
		this.ptr = ptr;
	}
	
	@Override
	public int getElem(int bank, int i) {
		if(ptr == 0)
			throw new IllegalStateException();
		if(ptr == -1)
			return 0;
		return FrameBuffer.readRGB(ptr, i);
	}

	@Override
	public void setElem(int bank, int i, int val) {
		if(ptr == 0)
			throw new IllegalStateException();
		if(ptr == -1)
			return;
		FrameBuffer.writeRGB(ptr, i, val);
	}
	
	public void close() {
		if(ptr != 0) {
			if(ptr != -1)
				FrameBuffer.closeDevice(ptr);
			ptr = 0;
		}
	}

	public int getWidth() {
		if(ptr == 0)
			throw new IllegalStateException();
		if(ptr == -1)
			return 1;
		return FrameBuffer.getDeviceWidth(ptr);
	}
	
	public int getHeight() {
		if(ptr == 0)
			throw new IllegalStateException();
		if(ptr == -1)
			return 1;
		return FrameBuffer.getDeviceHeight(ptr);
	}
}
