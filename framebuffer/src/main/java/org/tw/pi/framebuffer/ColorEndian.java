package org.tw.pi.framebuffer;

import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DirectColorModel;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;

public enum ColorEndian {
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
		return new DirectColorModel(
				ColorSpace.getInstance(ColorSpace.CS_sRGB),
				bpp,
				getComponentMask(bpp, ColorEndian.RED_COMPONENT),
				getComponentMask(bpp, ColorEndian.GREEN_COMPONENT),
				getComponentMask(bpp, ColorEndian.BLUE_COMPONENT),
				0x0,
				false,
				DataBuffer.TYPE_INT);
	}
	public SampleModel createSampleModel(int w, int h, int bpp) {
		return new SinglePixelPackedSampleModel(
				DataBuffer.TYPE_INT, 
				w, 
				h, 
				new int[] {
						getComponentMask(bpp, ColorEndian.RED_COMPONENT),
						getComponentMask(bpp, ColorEndian.GREEN_COMPONENT),
						getComponentMask(bpp, ColorEndian.BLUE_COMPONENT),
				});
	}
}