
package org.tw.pi.framebuffer;

import java.awt.image.BufferedImage;
import java.io.IOException;

import org.tw.pi.NarSystem;

public abstract class FrameBuffers {
	public static BufferedImage open(String fbdev) throws IOException {
		return open(ColorEndian.RGB, fbdev);
	}
	
	public static BufferedImage open(ColorEndian ce, String fbdev) throws IOException {
		return new FrameBufferedImage(fbdev);
	}

	static final long DUMMY = -1;
	static final long ERR_OPEN = 1;
	static final long ERR_FIXED = 2;
	static final long ERR_VARIABLE = 3;
	static final long ERR_BITS = 4;
	static final long ERR_MMAP = 5;

	static native long openDevice0(String fbdev);
	static native void closeDevice0(long ptr);

	static native int getDeviceWidth0(long ptr);
	static native int getDeviceHeight0(long ptr);
	static native int getDeviceBitsPerPixel0(long ptr);

	static native void writeRGB0(long ptr, int idx, int rgb);
	static native int readRGB0(long ptr, int idx);

	static long openDevice(String fbdev) throws IOException {
		long ptr = FrameBuffers.openDevice0(fbdev);

		if(ptr == FrameBuffers.DUMMY)
			return FrameBuffers.DUMMY;
		if(ptr == FrameBuffers.ERR_OPEN)
			throw new IOException("Unable to open framebuffer device: " + fbdev);
		if(ptr == FrameBuffers.ERR_FIXED)
			throw new IOException("Error reading fixed screen info for: " + fbdev);
		if(ptr == FrameBuffers.ERR_VARIABLE)
			throw new IOException("Error reading variable screen info for: " + fbdev);
		if(ptr == FrameBuffers.ERR_BITS)
			throw new IOException("Invalid color depth (" + FrameBuffers.getDeviceBitsPerPixel0(ptr) + "); 8, 16, and 24 supported; for: " + fbdev);
		if(ptr == FrameBuffers.ERR_MMAP)
			throw new IOException("Unable to mmap for: " + fbdev);

		return ptr;
	}

	private FrameBuffers() {
	}

	static {
		NarSystem.loadLibrary();
	}
}
