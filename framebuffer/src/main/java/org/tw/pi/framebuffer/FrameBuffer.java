/*
 *	This file is the JNI Java part of a Raspberry Pi FrameBuffer project.
 *
 *	Created 2013 by Thomas Welsch (ttww@gmx.de).
 *
 *	Do whatever you want to do with it :-)
 *
 **/

package org.tw.pi.framebuffer;

import org.tw.pi.NarSystem;


/**
 * This class is the Java front end for a simple to use FrameBuffer driver.
 * Simple draw in the BufferedImage and all changes are transfered to the FrameBuffer device.<p>
 * For testing purpose a dummy device is supported (via the devicename "dummy_160x128" instead of "/dev/fb1").<p<
 * The Java process needs write access to the frame buffer device file.
 * <p>
 * It's used to drive small bit mapped screens connected via SPI, see
 * http://www.sainsmart.com/blog/ada/
 * <p>
 * <p>
 * My Linux kernel config for SPI display was:
 * <pre>
 * CONFIG_FB_ST7735=y
 * CONFIG_FB_ST7735_PANEL_TYPE_RED_TAB=y
 * CONFIG_FB_ST7735_RGB_ORDER_REVERSED=y
 * CONFIG_FB_ST7735_MAP=y
 * CONFIG_FB_ST7735_MAP_RST_GPIO=25
 * CONFIG_FB_ST7735_MAP_DC_GPIO=24
 * CONFIG_FB_ST7735_MAP_SPI_BUS_NUM=0
 * CONFIG_FB_ST7735_MAP_SPI_BUS_CS=0
 * CONFIG_FB_ST7735_MAP_SPI_BUS_SPEED=16000000
 * CONFIG_FB_ST7735_MAP_SPI_BUS_MODE=0
 * </pre>
 * CONFIG_FB_ST7735_MAP_SPI_BUS_SPEED gives faster updates :-)
 * <p>
 * If you get the wrong colors, try the CONFIG_FB_ST7735_RGB_ORDER_REVERSED option !
 */
public abstract class FrameBuffer {

	public static native long		openDevice(String device);
	public static native void		closeDevice(long di);
	public static native int		getDeviceWidth(long di);
	public static native int		getDeviceHeight(long di);
	public static native int		getDeviceBitsPerPixel(long di);
	public static native void writeRGB(long di, int idx, int rgb);
	public static native int readRGB(long di, int idx);
	
	static {
		NarSystem.loadLibrary();
	}

	private FrameBuffer() {}
}
