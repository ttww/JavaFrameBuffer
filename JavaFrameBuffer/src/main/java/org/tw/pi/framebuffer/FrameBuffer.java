/*
 *	This file is the JNI Java part of a Raspberry Pi FrameBuffer project.
 *
 *	Created 2013 by Thomas Welsch (ttww@gmx.de).
 *
 *	Do whatever you want to do with it :-)
 *
 **/

package org.tw.pi.framebuffer;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.Closeable;

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
public class FrameBuffer implements Closeable {

	private static final int FPS = 60;		// Max. update rate

	private	String			deviceName;

	private long			deviceInfo;		// Private data from JNI C

	private	int				width,height;
	private	int				bits;

	private BufferedImage	img;
	private int[]			imgBuffer;

	// -----------------------------------------------------------------------------------------------------------------

	private static native long		openDevice(String device);
	private static native void		closeDevice(long di);
	private static native int		getDeviceWidth(long di);
	private static native int		getDeviceHeight(long di);
	private static native int		getDeviceBitsPerPixel(long di);
	private static native boolean	updateDeviceBuffer(long di,int[] buffer);

	static {
		NarSystem.loadLibrary();
	}

	// -----------------------------------------------------------------------------------------------------------------

	/**
	 * Open the named frame buffer device.
	 * 
	 * @param deviceName	e.g. /dev/fb1 or dummy_320x200
	 */
	public FrameBuffer(String deviceName) {

		this.deviceName = deviceName;

		deviceInfo = openDevice(deviceName);

		if (deviceInfo < 10) {
			throw new IllegalArgumentException("Init. for frame buffer "+deviceName+" failed with error code "+deviceInfo);
		}

		this.width	= getDeviceWidth(deviceInfo);
		this.height	= getDeviceHeight(deviceInfo);
		this.bits = getDeviceBitsPerPixel(deviceInfo);

		// We always use ARGB image type.
		img			= new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		imgBuffer	= ((DataBufferInt) img.getRaster().getDataBuffer()).getBankData()[0];
	}

	// -----------------------------------------------------------------------------------------------------------------

	// -----------------------------------------------------------------------------------------------------------------

	

	// -----------------------------------------------------------------------------------------------------------------

	// -----------------------------------------------------------------------------------------------------------------
	
	/**
	 * Update the screen.
	 * 
	 * @return	true if the BufferedImage was changed since the last call.
	 */
	public synchronized boolean write() {
		if (deviceInfo == 0) return false;
		return updateDeviceBuffer(deviceInfo,imgBuffer);
	}
	/**
	 * Close the device.
	 */
	public synchronized void close() {
		if(deviceInfo == 0)
			return;
		try {
			closeDevice(deviceInfo);
		} finally {
			deviceInfo = 0;
			img	= null;
			imgBuffer = null;
		}
	}

	// -----------------------------------------------------------------------------------------------------------------

	

	// -----------------------------------------------------------------------------------------------------------------
	
	// -----------------------------------------------------------------------------------------------------------------
	
	/**
	 * Returns the BufferedImage for drawing. Anything your draw here is synchronized to the frame buffer.
	 *
	 * @return	BufferedImage of type ARGB.
	 */
	public BufferedImage getBufferedImage() {
		return img;
	}
	public String getDeviceName() {
		return deviceName;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public int getBits() {
		return bits;
	}

}	// of class
