package org.tw.pi.framebuffer;

import java.awt.image.DataBuffer;

public class FrameBufferDataBuffer extends DataBuffer {
	
	static long open(String fbdev) {
		long ptr = FrameBuffer.openDevice(fbdev);

		if (ptr < 10) {
			throw new IllegalArgumentException("Init. for frame buffer " + fbdev + " failed with error code " + ptr);
		}
		
		return ptr;
	}

	private long ptr;
	private int w;
	private int h;
	
	private int[] buf;
	
	public FrameBufferDataBuffer(String fbdev) {
		this(open(fbdev));
	}
	
	public FrameBufferDataBuffer(int w, int h) {
		super(TYPE_INT, w * h);
		ptr = -1;
		this.w = w;
		this.h = h;
		buf = new int[w * h];
	}
	
	private FrameBufferDataBuffer(long ptr) {
		super(TYPE_INT, ptr == -1 ? 320*240 : FrameBuffer.getDeviceWidth(ptr) * FrameBuffer.getDeviceHeight(ptr));
		this.ptr = ptr;
		w = ptr == -1 ? 320 : FrameBuffer.getDeviceWidth(ptr);
		h = ptr == -1 ? 240 : FrameBuffer.getDeviceHeight(ptr);
		if(ptr == -1)
			this.buf = new int[w * h];
	}
	
	@Override
	public int getElem(int bank, int i) {
		if(ptr == 0)
			throw new IllegalStateException();
		if(ptr == -1)
			return buf[i];
		return FrameBuffer.readRGB(ptr, i);
	}

	@Override
	public void setElem(int bank, int i, int val) {
		if(ptr == 0)
			throw new IllegalStateException();
		if(ptr == -1)
			buf[i] = val;
		else
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
		return w;
	}
	
	public int getHeight() {
		return h;
	}
}
