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
	public static final long DUMMY = -1;
	public static final long ERR_OPEN = 1;
	public static final long ERR_FIXED = 2;
	public static final long ERR_VARIABLE = 3;
	public static final long ERR_BITS = 4;
	public static final long ERR_MMAP = 5;

	public static enum ColorEndian {
		RGB {
			@Override
			public ColorModel createColorModel(int bpp) {
				return createColorModelRGB(bpp);
			}

			@Override
			public SampleModel createSampleModel(int w, int h, int bpp) {
				return createSampleModelRGB(w, h, bpp);
			}
		},
		BGR {
			@Override
			public ColorModel createColorModel(int bpp) {
				return createColorModelBGR(bpp);
			}

			@Override
			public SampleModel createSampleModel(int w, int h, int bpp) {
				return createSampleModelBGR(w, h, bpp);
			}
		},
		;
		
		public abstract ColorModel createColorModel(int bpp);
		public abstract SampleModel createSampleModel(int w, int h, int bpp);
	}
	
	public static ColorModel createColorModelRGB8() {
		return new DirectColorModel(
				ColorSpace.getInstance(ColorSpace.CS_sRGB),
				16,
				0x000000e0,
				0x0000001c,
				0x00000003,
				0x0,
				false,
				DataBuffer.TYPE_INT);
	}
	
	public static ColorModel createColorModelRGB16() {
		return new DirectColorModel(
				ColorSpace.getInstance(ColorSpace.CS_sRGB),
				16,
				0x0000f800,
				0x000007e0,
				0x0000001f,
				0x0,
				false,
				DataBuffer.TYPE_INT);
	}
	
	public static ColorModel createColorModelRGB24() {
		return new DirectColorModel(
				ColorSpace.getInstance(ColorSpace.CS_sRGB),
				24,
				0x00ff0000,
				0x0000ff00,
				0x000000ff,
				0x0,
				false,
				DataBuffer.TYPE_INT);
	}
	
	public static ColorModel createColorModelBGR8() {
		return new DirectColorModel(
				ColorSpace.getInstance(ColorSpace.CS_sRGB),
				16,
				0x00000003,
				0x0000001c,
				0x000000e0,
				0x0,
				false,
				DataBuffer.TYPE_INT);
	}
	
	public static ColorModel createColorModelBGR16() {
		return new DirectColorModel(
				ColorSpace.getInstance(ColorSpace.CS_sRGB),
				16,
				0x0000001f,
				0x000007e0,
				0x0000f800,
				0x0,
				false,
				DataBuffer.TYPE_INT);
	}
	
	public static ColorModel createColorModelBGR24() {
		return new DirectColorModel(
				ColorSpace.getInstance(ColorSpace.CS_sRGB),
				24,
				0x000000ff,
				0x0000ff00,
				0x00ff0000,
				0x0,
				false,
				DataBuffer.TYPE_INT);
	}
	
	public static ColorModel createColorModelRGB(int bpp) {
		switch(bpp) {
		case 8:
			return createColorModelRGB8();
		case 16:
			return createColorModelRGB16();
		case 24:
			return createColorModelRGB24();
		}
		throw new IllegalArgumentException("Invalid RGB color depth:" + bpp);
	}

	public static ColorModel createColorModelBGR(int bpp) {
		switch(bpp) {
		case 8:
			return createColorModelBGR8();
		case 16:
			return createColorModelBGR16();
		case 24:
			return createColorModelBGR24();
		}
		throw new IllegalArgumentException("Invalid BGR color depth:" + bpp);
	}

	public static final SampleModel createSampleModelRGB8(int w, int h) {
		return new SinglePixelPackedSampleModel(
				DataBuffer.TYPE_INT, 
				w, 
				h, 
				new int[] {
						0x000000e0,
						0x0000001c,
						0x00000003,
				});
	}

	public static final SampleModel createSampleModelRGB16(int w, int h) {
		return new SinglePixelPackedSampleModel(
				DataBuffer.TYPE_INT, 
				w, 
				h, 
				new int[] {
						0x0000f800,
						0x000007e0,
						0x0000001f,
				});
	}

	public static final SampleModel createSampleModelRGB24(int w, int h) {
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

	public static final SampleModel createSampleModelBGR8(int w, int h) {
		return new SinglePixelPackedSampleModel(
				DataBuffer.TYPE_INT, 
				w, 
				h, 
				new int[] {
						0x00000003,
						0x0000001c,
						0x000000e0,
				});
	}

	public static final SampleModel createSampleModelBGR16(int w, int h) {
		return new SinglePixelPackedSampleModel(
				DataBuffer.TYPE_INT, 
				w, 
				h, 
				new int[] {
						0x0000001f,
						0x000007e0,
						0x0000f800,
				});
	}

	public static final SampleModel createSampleModelBGR24(int w, int h) {
		return new SinglePixelPackedSampleModel(
				DataBuffer.TYPE_INT, 
				w, 
				h, 
				new int[] {
						0x000000ff,
						0x0000ff00,
						0x00ff0000,
				});
	}

	public static SampleModel createSampleModelRGB(int w, int h, int bpp) {
		switch(bpp) {
		case 8:
			return createSampleModelRGB8(w, h);
		case 16:
			return createSampleModelRGB16(w, h);
		case 24:
			return createSampleModelRGB24(w, h);
		}
		throw new IllegalArgumentException("Invalid RGB color depth:" + bpp);
	}

	public static SampleModel createSampleModelBGR(int w, int h, int bpp) {
		switch(bpp) {
		case 8:
			return createSampleModelBGR8(w, h);
		case 16:
			return createSampleModelBGR16(w, h);
		case 24:
			return createSampleModelBGR24(w, h);
		}
		throw new IllegalArgumentException("Invalid BGR color depth:" + bpp);
	}

	public static native long openDevice(String fbdev);
	public static native void closeDevice(long ptr);

	public static native int getDeviceWidth(long ptr);
	public static native int getDeviceHeight(long ptr);
	public static native int getDeviceBitsPerPixel(long ptr);

	public static native void writeRGB(long ptr, int idx, int rgb);
	public static native int readRGB(long ptr, int idx);

	public static long open(String fbdev) {
		long ptr = FrameBuffers.openDevice(fbdev);

		if(ptr == FrameBuffers.DUMMY)
			return FrameBuffers.DUMMY;
		if(ptr == FrameBuffers.ERR_OPEN)
			throw new RuntimeException("Unable to open framebuffer device: " + fbdev);
		if(ptr == FrameBuffers.ERR_FIXED)
			throw new RuntimeException("Error reading fixed screen info for: " + fbdev);
		if(ptr == FrameBuffers.ERR_VARIABLE)
			throw new RuntimeException("Error reading variable screen info for: " + fbdev);
		if(ptr == FrameBuffers.ERR_BITS)
			throw new RuntimeException("Invalid color depth (" + FrameBuffers.getDeviceBitsPerPixel(ptr) + "), 16 and 24 supported, for: " + fbdev);
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
