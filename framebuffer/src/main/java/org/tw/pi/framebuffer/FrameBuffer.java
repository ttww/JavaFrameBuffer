/*
 *	This file is the JNI Java part of a Raspberry Pi FrameBuffer project.
 *
 *	Created 2013 by Thomas Welsch (ttww@gmx.de).
 *
 *	Do whatever you want to do with it :-)
 *
 **/

package org.tw.pi.framebuffer;

import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DirectColorModel;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;

import org.tw.pi.NarSystem;

public abstract class FrameBuffer {
	public static final long DUMMY = -1;
	static final long ERR_OPEN = 1;
	static final long ERR_FIXED = 2;
	static final long ERR_VARIABLE = 3;
	static final long ERR_BITS = 4;
	static final long ERR_MMAP = 5;

	static native long openDevice(String fbdev);
	static native void closeDevice(long ptr);

	static native int getDeviceWidth(long ptr);
	static native int getDeviceHeight(long ptr);
	static native int getDeviceBitsPerPixel(long ptr);

	static native void writeRGB(long ptr, int idx, int rgb);
	static native int readRGB(long ptr, int idx);

	static {
		NarSystem.loadLibrary();
	}
	
	public static long open(String fbdev) {
		long ptr = FrameBuffer.openDevice(fbdev);

		if(ptr == FrameBuffer.DUMMY)
			return FrameBuffer.DUMMY;
		if(ptr == FrameBuffer.ERR_OPEN)
			throw new RuntimeException("Unable to open framebuffer device: " + fbdev);
		if(ptr == FrameBuffer.ERR_FIXED)
			throw new RuntimeException("Error reading fixed screen info for: " + fbdev);
		if(ptr == FrameBuffer.ERR_VARIABLE)
			throw new RuntimeException("Error reading variable screen info for: " + fbdev);
		if(ptr == FrameBuffer.ERR_BITS)
			throw new RuntimeException("Invalid color depth (" + FrameBuffer.getDeviceBitsPerPixel(ptr) + "), 16 and 24 supported, for: " + fbdev);
		if(ptr == FrameBuffer.ERR_MMAP)
			throw new RuntimeException("Unable to mmap for: " + fbdev);

		return ptr;
	}

	public static ColorModel createColorModel(int bpp) {
		if(bpp == 16) {
			return new DirectColorModel(16,
	                0x00f80000,   // Red
	                0x0000fc00,   // Green
	                0x000000f8,   // Blue
	                0x0           // Alpha
	                );
		} else if(bpp == 24) {
			return new DirectColorModel(24,
	                0x00ff0000,   // Red
	                0x0000ff00,   // Green
	                0x000000ff,   // Blue
	                0x0           // Alpha
	                );
		} else
			throw new IllegalArgumentException("Invalid framebuffer color depth:" + bpp);
	}

	public static SampleModel createSampleModel(int bpp, int w, int h) {
		if(bpp == 16) {
			return new SinglePixelPackedSampleModel(
					DataBuffer.TYPE_INT, 
					w, 
					h, 
					new int[] {
			                0x00f80000,   // Red
			                0x0000fc00,   // Green
			                0x000000f8,   // Blue
					});
		} else if(bpp == 24) {
			return new SinglePixelPackedSampleModel(
					DataBuffer.TYPE_INT, 
					w, 
					h, 
					new int[] {
			                0x00ff0000,   // Red
			                0x0000ff00,   // Green
			                0x000000ff,   // Blue
					});
		} else
			throw new IllegalArgumentException("Invalid framebuffer color depth:" + bpp);
	}

	private FrameBuffer() {
	}
}
