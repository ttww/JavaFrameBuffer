package org.tw.pi.framebuffer;

import java.awt.image.DataBuffer;

public class FrameBuffer extends DataBuffer {
	private static final int DEFAULT_DUMMY_WIDTH = 320;
	private static final int DEFAULT_DUMMY_HEIGHT = 240;
	private static final int DEFAULT_COLOR_DEPTH = 24;
	
	private final long ptr;
	private final int w;
	private final int h;
	private final int bpp;

	private final int[] dummy;
	
	private boolean closed;

	public FrameBuffer() {
		this(FrameBuffers.DUMMY);
	}
	
	public FrameBuffer(String fbdev) {
		this(FrameBuffers.open(fbdev));
	}

	public FrameBuffer(int w, int h, int bpp) {
		this(FrameBuffers.DUMMY, w, h, bpp);
	}

	public FrameBuffer(int w, int h) {
		this(FrameBuffers.DUMMY, w, h, DEFAULT_COLOR_DEPTH);
	}

	private FrameBuffer(long ptr) {
		this(
				ptr,
				(ptr == FrameBuffers.DUMMY ? DEFAULT_DUMMY_WIDTH : FrameBuffers.getDeviceWidth(ptr)), 
				(ptr == FrameBuffers.DUMMY ? DEFAULT_DUMMY_HEIGHT : FrameBuffers.getDeviceHeight(ptr)),
				(ptr == FrameBuffers.DUMMY ? DEFAULT_COLOR_DEPTH : FrameBuffers.getDeviceBitsPerPixel(ptr)));
	}

	private FrameBuffer(long ptr, int w, int h, int bpp) {
		super(TYPE_INT, w * h);
		this.ptr = ptr;
		this.w = w;
		this.h = h;
		if(ptr == FrameBuffers.DUMMY) {
			this.bpp = bpp;
			this.dummy = new int[w * h];
		} else {
			this.bpp = FrameBuffers.getDeviceBitsPerPixel(ptr);
			this.dummy = null;
		}
		this.closed = false;
	}

	@Override
	public int getElem(int bank, int i) {
		if(closed)
			throw new IllegalStateException();
		int val;
		if(ptr == FrameBuffers.DUMMY)
			val = dummy[i];
		else
			val = FrameBuffers.readRGB(ptr, i);
		return val & 0x00FFFFFF;
	}

	@Override
	public void setElem(int bank, int i, int val) {
		val = val & 0x00FFFFFF;
		if(closed)
			throw new IllegalStateException();
		if(ptr == FrameBuffers.DUMMY)
			dummy[i] = val;
		else
			FrameBuffers.writeRGB(ptr, i, val);
	}

	public void close() {
		if(!closed) {
			if(ptr != FrameBuffers.DUMMY)
				FrameBuffers.closeDevice(ptr);
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
