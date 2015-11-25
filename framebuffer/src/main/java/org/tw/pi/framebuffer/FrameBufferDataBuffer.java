package org.tw.pi.framebuffer;

import java.awt.image.DataBuffer;

public class FrameBufferDataBuffer extends DataBuffer {
	private static final int DEFAULT_DUMMY_WIDTH = 320;
	private static final int DEFAULT_DUMMY_HEIGHT = 240;
	private static final int DEFAULT_COLOR_DEPTH = 24;
	
	private final long ptr;
	private final int w;
	private final int h;
	private final int bpp;

	private final int[] dummy;
	
	private boolean closed;

	public FrameBufferDataBuffer() {
		this(FrameBuffer.DUMMY);
	}
	
	public FrameBufferDataBuffer(String fbdev) {
		this(FrameBuffer.open(fbdev));
	}

	public FrameBufferDataBuffer(int w, int h, int bpp) {
		this(FrameBuffer.DUMMY, w, h, bpp);
	}

	public FrameBufferDataBuffer(int w, int h) {
		this(FrameBuffer.DUMMY, w, h, DEFAULT_COLOR_DEPTH);
	}

	private FrameBufferDataBuffer(long ptr) {
		this(
				ptr,
				(ptr == FrameBuffer.DUMMY ? DEFAULT_DUMMY_WIDTH : FrameBuffer.getDeviceWidth(ptr)), 
				(ptr == FrameBuffer.DUMMY ? DEFAULT_DUMMY_HEIGHT : FrameBuffer.getDeviceHeight(ptr)),
				(ptr == FrameBuffer.DUMMY ? DEFAULT_COLOR_DEPTH : FrameBuffer.getDeviceBitsPerPixel(ptr)));
	}

	private FrameBufferDataBuffer(long ptr, int w, int h, int bpp) {
		super(TYPE_INT, w * h);
		this.ptr = ptr;
		this.w = w;
		this.h = h;
		if(ptr == FrameBuffer.DUMMY) {
			this.bpp = bpp;
			this.dummy = new int[w * h];
		} else {
			this.bpp = FrameBuffer.getDeviceBitsPerPixel(ptr);
			this.dummy = null;
		}
		this.closed = false;
	}

	@Override
	public int getElem(int bank, int i) {
		if(closed)
			throw new IllegalStateException();
		int val;
		if(ptr == FrameBuffer.DUMMY)
			val = dummy[i];
		else
			val = FrameBuffer.readRGB(ptr, i);
		return val & 0x00FFFFFF;
	}

	@Override
	public void setElem(int bank, int i, int val) {
		val = val & 0x00FFFFFF;
		if(closed)
			throw new IllegalStateException();
		if(ptr == FrameBuffer.DUMMY)
			dummy[i] = val;
		else
			FrameBuffer.writeRGB(ptr, i, val);
	}

	public void close() {
		if(!closed) {
			if(ptr != FrameBuffer.DUMMY)
				FrameBuffer.closeDevice(ptr);
			closed = true;
		}
	}

	public int getWidth() {
		return w;
	}

	public int getHeight() {
		return h;
	}
	
	public int getColorDepth() {
		return bpp;
	}
}
