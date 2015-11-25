/*
 *	This file is the JNI Java part of a Raspberry Pi FrameBuffer project.
 *
 *	Created 2013 by Thomas Welsch (ttww@gmx.de).
 *
 *	Do whatever you want to do with it :-)
 *
 **/

package org.tw.pi.framebuffer;

import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DirectColorModel;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;

import org.tw.pi.NarSystem;

public abstract class FrameBuffers {
	static final long DUMMY = -1;
	static final long ERR_OPEN = 1;
	static final long ERR_FIXED = 2;
	static final long ERR_VARIABLE = 3;
	static final long ERR_BITS = 4;
	static final long ERR_MMAP = 5;

	public static enum ColorEndian {
		RGB(new int[]{
				0x000000e0,
				0x0000001c,
				0x00000003,
		}, new int[]{
				0x0000f800,
				0x000007e0,
				0x0000001f,
		}, new int[] {
				0x00ff0000,
				0x0000ff00,
				0x000000ff,
		}),
		BGR(new int[]{
				0x00000003,
				0x0000001c,
				0x000000e0,
		}, new int[]{
				0x0000001f,
				0x000007e0,
				0x0000f800,
		}, new int[] {
				0x000000ff,
				0x0000ff00,
				0x00ff0000,
		}),
		;

		public static final int RED_COMPONENT = 0;
		public static final int GREEN_COMPONENT = 1;
		public static final int BLUE_COMPONENT = 2;
		
		private int[] mask8;
		private int[] mask16;
		private int[] mask24;

		private ColorEndian(int[] mask8, int[] mask16, int[] mask24) {
			this.mask8 = mask8;
			this.mask16 = mask16;
			this.mask24 = mask24;
		}
		
		public int getComponentMask(int bpp, int component) {
			switch(bpp) {
			case 8: return mask8[component];
			case 16: return mask16[component];
			case 24: return mask24[component];
			}
			throw new IllegalArgumentException("Invalid color depth for " + this + ": " + bpp);
		}

		public ColorModel createColorModel(int bpp) {
			return FrameBuffers.createColorModel(this, bpp);
		}
		public SampleModel createSampleModel(int w, int h, int bpp) {
			return FrameBuffers.createSampleModel(this, w, h, bpp);
		}
	}

	public static ColorModel createColorModel(ColorEndian ce, int bpp) {
		return new DirectColorModel(
				ColorSpace.getInstance(ColorSpace.CS_sRGB),
				bpp,
				ce.getComponentMask(bpp, ColorEndian.RED_COMPONENT),
				ce.getComponentMask(bpp, ColorEndian.GREEN_COMPONENT),
				ce.getComponentMask(bpp, ColorEndian.BLUE_COMPONENT),
				0x0,
				false,
				DataBuffer.TYPE_INT);
	}


	public static SampleModel createSampleModel(ColorEndian ce, int w, int h, int bpp) {
		return new SinglePixelPackedSampleModel(
				DataBuffer.TYPE_INT, 
				w, 
				h, 
				new int[] {
						ce.getComponentMask(bpp, ColorEndian.RED_COMPONENT),
						ce.getComponentMask(bpp, ColorEndian.GREEN_COMPONENT),
						ce.getComponentMask(bpp, ColorEndian.BLUE_COMPONENT),
				});
	}
	
	public static FrameBufferedImage openImage(String fbdev) {
		return new FrameBufferedImage(fbdev);
	}

	public static FrameBufferedImage openImage(ColorEndian ce, String fbdev) {
		return new FrameBufferedImage(ce, fbdev);
	}

	static native long openDevice0(String fbdev);
	static native void closeDevice0(long ptr);

	static native int getDeviceWidth0(long ptr);
	static native int getDeviceHeight0(long ptr);
	static native int getDeviceBitsPerPixel0(long ptr);

	static native void writeRGB0(long ptr, int idx, int rgb);
	static native int readRGB0(long ptr, int idx);

	static long openDevice(String fbdev) {
		long ptr = FrameBuffers.openDevice0(fbdev);

		if(ptr == FrameBuffers.DUMMY)
			return FrameBuffers.DUMMY;
		if(ptr == FrameBuffers.ERR_OPEN)
			throw new RuntimeException("Unable to open framebuffer device: " + fbdev);
		if(ptr == FrameBuffers.ERR_FIXED)
			throw new RuntimeException("Error reading fixed screen info for: " + fbdev);
		if(ptr == FrameBuffers.ERR_VARIABLE)
			throw new RuntimeException("Error reading variable screen info for: " + fbdev);
		if(ptr == FrameBuffers.ERR_BITS)
			throw new RuntimeException("Invalid color depth (" + FrameBuffers.getDeviceBitsPerPixel0(ptr) + "), 16 and 24 supported, for: " + fbdev);
		if(ptr == FrameBuffers.ERR_MMAP)
			throw new RuntimeException("Unable to mmap for: " + fbdev);

		return ptr;
	}

	private FrameBuffers() {
	}

	static {
		NarSystem.loadLibrary();
	}
}
