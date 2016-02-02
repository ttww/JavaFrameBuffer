/*
 *	This file is the JNI Java part of a Raspberry Pi FrameBuffer project.
 *
 *	Created 2013 by Thomas Welsch (ttww@gmx.de).
 *
 *	Do whatever you want to do with it :-)
 *
 **/

package org.tw.pi.framebuffer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.concurrent.ArrayBlockingQueue;

import javax.swing.JPanel;


/**
 * This class is the Java front end for a simple to use FrameBuffer driver.
 * Simple draw in the BufferedImage and all changes are transfered to the FrameBuffer device.<p>
 * For testing purpose a dummy device is supported (via the devicename "dummy_160x128" instead of "/dev/fb1").<p<
 * The Java process needs write access to the frame buffer device file.
 * <p/>
 * It's used to drive small bit mapped screens connected via SPI, see
 * http://www.sainsmart.com/blog/ada/
 * <p/>
 * <p/>
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
 * <p/>
 * If you get the wrong colors, try the CONFIG_FB_ST7735_RGB_ORDER_REVERSED option !
 */
public class FrameBuffer {

	private static final int FPS = 40;        // Max. update rate

	private String deviceName;

	private long deviceInfo;        // Private data from JNI C

	private int width, height;
	private int bits;

	private BufferedImage img;
	private int[] imgBuffer;

	private ManualRepaintThread	mrt;

	// -----------------------------------------------------------------------------------------------------------------

	private native long openDevice(String device);

	private native void closeDevice(long di);

	private native int getDeviceWidth(long di);

	private native int getDeviceHeight(long di);

	private native int getDeviceBitsPerPixel(long di);

	private native boolean updateDeviceBuffer(long di, int[] buffer);

	static {
		System.loadLibrary("FrameBufferJNI"); // FrameBufferJNI.dll (Windows) or FrameBufferJNI.so (Unixes)
	}

	// -----------------------------------------------------------------------------------------------------------------

	/**
	 * Open the named frame buffer device and starts the automatic update thread between the internal
	 * BufferedImage and the device.
	 *
	 * @param deviceName e.g. /dev/fb1 or dummy_320x200
	 */
	public FrameBuffer(String deviceName) {
		this(deviceName, true);
	}

	// -----------------------------------------------------------------------------------------------------------------

	/**
	 * Open the named frame buffer device.
	 *
	 * @param deviceName e.g. /dev/fb1 or dummy_320x200
	 * @param autoUpdate if true, starts the automatic update thread between the internal
	 *                   BufferedImage and the device. If false, you have to call repaint();
	 */
	public FrameBuffer(String deviceName, boolean autoUpdate) {

		this.deviceName = deviceName;

		deviceInfo = openDevice(deviceName);

		if (Math.abs(deviceInfo) < 10) {
			throw new IllegalArgumentException("Init. for frame buffer " + deviceName + " failed with error code " + deviceInfo);
		}

		this.width  = getDeviceWidth(deviceInfo);
		this.height = getDeviceHeight(deviceInfo);

		System.err.println("Open with " + deviceName + " (" + deviceInfo + ")");
		System.err.println("  width   " + getDeviceWidth(deviceInfo));
		System.err.println("  height  " + getDeviceHeight(deviceInfo));
		System.err.println("  bpp     " + getDeviceBitsPerPixel(deviceInfo));

		// We always use ARGB image type.
		img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		imgBuffer = ((DataBufferInt) img.getRaster().getDataBuffer()).getBankData()[0];

		if (autoUpdate)
			new AutoUpdateThread().start();
		else {
			mrt = new ManualRepaintThread();
			mrt.start();
		}
	}

	// -----------------------------------------------------------------------------------------------------------------

	private ScreenPanel screenPanel;

	/**
	 * Returns a ScreenPanel (JPanel) which represents the actual frame buffer device.
	 *
	 * @return ScreenPanel...
	 */
	public ScreenPanel getScreenPanel() {
		synchronized (deviceName) {
			if (screenPanel != null) throw new IllegalStateException("Only one screen panel supported");

			screenPanel = new ScreenPanel();

			return screenPanel;
		}
	}

	// -----------------------------------------------------------------------------------------------------------------

	/**
	 * Internal helper class for displaying the current frame buffer image via a JPanel.
	 */
	@SuppressWarnings("serial")
	public class ScreenPanel extends JPanel {

		private int scale = 1;

		public ScreenPanel() {
			setPreferredSize(new Dimension(FrameBuffer.this.width, FrameBuffer.this.height));
		}

		@Override
		protected void paintComponent(Graphics g) {
			synchronized (getUpdateLockForSync()) {
				super.paintComponent(g);

				int w  = this.getWidth();
				int h  = this.getHeight();
				int wi = img.getWidth() * scale;
				int hi = img.getHeight() * scale;

				Graphics2D g2 = (Graphics2D) g;
//				g2.translate(w / 2 - wi / 2, h / 2 - hi / 2);
				g2.translate((w - wi) / 2, (h - hi) / 2);
				g2.scale(scale, scale);

				g.setColor(Color.BLACK);
				g.fillRect(0, 0, img.getWidth(), img.getHeight());
				g.drawImage(img, 0, 0, null);
			}
		}

		public void setScale(int scale) {
			this.scale = scale;
			repaint();
		}

		public int componentToScreenX(int x) {
			int w =  this.getWidth();
			int wi = img.getWidth();
			int d = (int) (((w - wi * scale) / 2f));
			x = (x - d) / scale;
			if (x <  0)  x = 0;
			if (x >= wi) x = wi - 1;
			return x;
		}
		public int componentToScreenY(int y) {
			int h = this.getHeight();
			int hi = img.getHeight();
			int d = (int) (((h - hi * scale) / 2f));
			y = (y - d) / scale;
			if (y <  0)  y = 0;
			if (y >= hi) y = hi - 1;
			return y;
		}
	}

	// -----------------------------------------------------------------------------------------------------------------

	/**
	 * Internal helper class for refreshing the frame buffer display and/or JPanel.
	 */
	private class AutoUpdateThread extends Thread {

		AutoUpdateThread() {
			setDaemon(true);
			setName("FB " + deviceName + " update");
		}

		@Override
		public void run() {
			final int SLEEP_TIME = 1000 / FPS;

			// System.err.println("Run Update");
			while (deviceInfo != 0) {

				updateScreen();

				try {
					sleep(SLEEP_TIME);
				} catch (InterruptedException e) {
					break;
				}

			}    // while
		}

	}    // class UpdateThread

	// -----------------------------------------------------------------------------------------------------------------

	private ArrayBlockingQueue<Boolean> repaintQueue = new ArrayBlockingQueue<Boolean>(1);

	/**
	 * Request an repaint manually. This method can called at high frequencies. An internal repaint tread is used to
	 * avoid exceeding the FPS value.
	 */
	public void repaint() {
		if (mrt == null) throw new IllegalStateException("automatic repaint is active, no need to call this");
		repaintQueue.offer(Boolean.TRUE);
	}

	/**
	 * Internal helper class for refreshing the frame buffer display and/or JPanel.
	 */
	private class ManualRepaintThread extends Thread {

		ManualRepaintThread() {
			setDaemon(true);
			setName("FB " + deviceName + " repaint");
		}

		@Override
		public void run() {
			final int SLEEP_TIME = 1000 / FPS;

			try {
				System.err.println("Run Repaint");
				while (deviceInfo != 0) {

					repaintQueue.take();
					updateScreen();

					sleep(SLEEP_TIME);

				}    // while
			} catch (InterruptedException e) {
			}

		}    // class UpdateThread
	}

	// -----------------------------------------------------------------------------------------------------------------

	/**
	 * Returns the BufferedImage for drawing. Anything your draw here is synchronized to the frame buffer.
	 *
	 * @return BufferedImage of type ARGB.
	 */
	public BufferedImage getScreen() {
		return img;
	}

	// -----------------------------------------------------------------------------------------------------------------

	/**
	 * Close the device.
	 */
	public void close() {
		synchronized (deviceName) {
			closeDevice(deviceInfo);
			deviceInfo = 0;
			img = null;
			imgBuffer = null;
		}
	}

	// -----------------------------------------------------------------------------------------------------------------

	private long	lastUpdate;
	private	int		updateCount;

	/**
	 * Update the screen if no automatic sync is used (see constructor autoUpdate flag).
	 * This method is normally called by the autoUpdate thread and is not limited about any frame rate.
	 *
	 * @return true if the BufferedImage was changed since the last call.
	 */
	public boolean updateScreen() {


		synchronized (deviceName) {
			if (deviceInfo == 0) return false;

			boolean ret;
			synchronized (updateLock) {

				ret = updateDeviceBuffer(deviceInfo, imgBuffer);

				updateCount++;
				if (lastUpdate == 0) lastUpdate = System.currentTimeMillis();
				long now = System.currentTimeMillis();

				long diff = now - lastUpdate;

				if (diff >= 1000) {
					float fps = (1000f / diff) * updateCount;
//					System.err.println("FPS = "+fps);
					updateCount = 0;
					lastUpdate  = now;
				}

			}

			if (ret && screenPanel != null) {
				screenPanel.repaint();
			}
			return ret;
		}    // sync
	}

	// -----------------------------------------------------------------------------------------------------------------

	private Object updateLock = new Object();

	public Object getUpdateLockForSync() {
		return updateLock;
	}

	// -----------------------------------------------------------------------------------------------------------------

	public int getWidth() {
		return width;
	}

	// -----------------------------------------------------------------------------------------------------------------

	public int getHeight() {
		return height;
	}

	// -----------------------------------------------------------------------------------------------------------------

}    // of class
