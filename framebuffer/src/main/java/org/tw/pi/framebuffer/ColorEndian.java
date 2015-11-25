package org.tw.pi.framebuffer;

import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DirectColorModel;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;

/**
 * Utility enum useful as an argument to select between RGB and BGR framebuffers.<p>
 * 
 * Not sure if BGR framebuffers exist, but oh well.
 * @author Robin Kirkman
 *
 */
public enum ColorEndian {
	/**
	 * Color is represented as R, G, B, in ascending address order
	 */
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
	/**
	 * Color is represented as B, G, R, in ascending address order
	 */
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

	/**
	 * The red component offset in a bitmask array
	 */
	private static final int RED_COMPONENT = 0;
	/**
	 * The green component offset in a bitmask array
	 */
	private static final int GREEN_COMPONENT = 1;
	/**
	 * The blue component offset in a bitmask array
	 */
	private static final int BLUE_COMPONENT = 2;
	
	/**
	 * 8-bit color bitmasks for red, green, blue
	 */
	private int[] mask8;
	/**
	 * 16-bit color bitmasks for red, green, blue
	 */
	private int[] mask16;
	/**
	 * 24-bit color bitmasks for red, green, blue
	 */
	private int[] mask24;

	private ColorEndian(int[] mask8, int[] mask16, int[] mask24) {
		this.mask8 = mask8;
		this.mask16 = mask16;
		this.mask24 = mask24;
	}
	
	/**
	 * Get the bitmask for a component at a particular color depth
	 * @param bpp The color depth: {@code 8}, {@code 16}, or {@code 24}
	 * @param component The color component offset
	 * @return The bitmask for that color at that color depth
	 * @see #RED_COMPONENT
	 * @see #GREEN_COMPONENT
	 * @see #BLUE_COMPONENT
	 */
	private int getComponentMask(int bpp, int component) {
		switch(bpp) {
		case 8: return mask8[component];
		case 16: return mask16[component];
		case 24: return mask24[component];
		}
		throw new IllegalArgumentException("Invalid color depth for " + this + ": " + bpp);
	}

	/**
	 * Create and return a new {@link ColorModel} appropriate for use in a {@link FrameBufferedImage}
	 * at the argument color depth
	 * @param bpp The color depth: {@code 8}, {@code 16}, or {@code 24}
	 * @return A new {@link ColorModel}
	 */
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
	
	/**
	 * Create and return a new {@link SampleModel} appropriate for use in a {@link FrameBufferedImage}
	 * with the argument width, height, and color depth
	 * @param w The width
	 * @param h The height
	 * @param bpp The color depth: {@code 8}, {@code 16}, or {@code 24}
	 * @return A new {@link SampleModel}
	 */
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