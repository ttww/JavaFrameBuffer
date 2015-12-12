
package org.tw.pi.framebuffer;

import java.awt.image.BufferedImage;
import java.io.IOException;

import org.tw.pi.NarSystem;

/**
 * Utilities and JNI used by {@link FrameBuffer}.<p>
 * 
 * Also has utility methods {@link #open(String)} and {@link #open(ColorEndian, String)} to
 * return a new {@link BufferedImage} backed by a linux framebuffer.
 * @author Robin Kirkman
 *
 */
public abstract class FrameBuffers {
	/**
	 * Open a linux framebuffer device and return a new {@link BufferedImage} backed by it,
	 * using RGB color endian-ness
	 * @param fbdev The path to the framebuffer, such as {@code "/dev/fb1"}
	 * @return A new {@link BufferedImage} backed by the framebuffer
	 * @throws IOException If the linux framebuffer could not be opened
	 * @see ColorEndian#RGB
	 * @see FrameBufferedImage
	 */
	public static BufferedImage open(String fbdev) throws IOException {
		return open(ColorEndian.RGB, fbdev);
	}
	
	/**
	 * Open a linux framebuffer device and return a new {@link BufferedImage} backed by it,
	 * using a specified color endian-ness and framebuffer path
	 * @param ce The color endianness: {@link ColorEndian#RGB} or {@link ColorEndian#BGR}
	 * @param fbdev The path the the framebuffer, such as {@code "/dev/fb1"}
	 * @return A new {@link BufferedImage} backed by the framebuffer
	 * @throws IOException If the linux framebuffer could not be opened
	 * @see FrameBufferedImage
	 */
	public static BufferedImage open(ColorEndian ce, String fbdev) throws IOException {
		return new FrameBufferedImage(fbdev);
	}

	/**
	 * magic JNI struct pointer value to indicate a dummy {@link FrameBuffer}
	 */
	static final long DUMMY = -1;
	/**
	 * Returned by {@link #openDevice0(String)} on non-linux systems
	 */
	static final long ERR_NOT_SUPPORTED = 0;
	/**
	 * Returned by {@link #openDevice0(String)} if the framebuffer could not be opened
	 */
	static final long ERR_OPEN = 1;
	/**
	 * Returned by {@link #openDevice0(String)} if the "fixed" framebuffer data could not be read
	 */
	static final long ERR_FIXED = 2;
	/**
	 * Returned by {@link #openDevice0(String)} if the "variable" framebuffer data could not be read
	 */
	static final long ERR_VARIABLE = 3;
	/**
	 * Returned by {@link #openDevice0(String)} if the framebuffer's color depth is not 8, 16, or 24
	 */
	static final long ERR_BITS = 4;
	/**
	 * Returned by {@link #openDevice0(String)} if the framebuffer could not be memory mapped
	 */
	static final long ERR_MMAP = 5;

	/**
	 * Open a framebuffer by path and return a pointer of type {@code struct FrameBufferData*}
	 * @param fbdev The path to the framebuffer, such as {@code "/dev/fb1"}
	 * @return A JNI pointer, or one of the listed error codes.
	 * @see #ERR_NOT_SUPPORTED
	 * @see #ERR_OPEN
	 * @see #ERR_FIXED
	 * @see #ERR_VARIABLE
	 * @see #ERR_BITS
	 * @see #ERR_MMAP
	 */
	static native long openDevice0(String fbdev);
	
	/**
	 * Close a framebuffer referenced by a pointer of type {@code struct FrameBufferData*}.
	 * Will probably crash the JVM if the pointer is invalid.
	 * @param ptr A valid JNI pointer returned by {@link #openDevice0(String)}
	 */
	static native void closeDevice0(long ptr);

	/**
	 * Returns the width of a framebuffer
	 * @param ptr A valid JNI pointer returned by {@link #openDevice0(String)}
	 * @return The width in pixels
	 */
	static native int getDeviceWidth0(long ptr);
	/**
	 * Returns the height of a framebuffer
	 * @param ptr A valid JNI pointer returned by {@link #openDevice0(String)}
	 * @return The height in pixels
	 */
	static native int getDeviceHeight0(long ptr);
	/**
	 * Returns the color depth of a framebuffer
	 * @param ptr A valid JNI pointer returned by {@link #openDevice0(String)}
	 * @return The native framebuffer's color depth
	 */
	static native int getDeviceBitsPerPixel0(long ptr);

	/**
	 * Write a pixel to a native framebuffer
	 * @param ptr A valid JNI pointer returned by {@link #openDevice0(String)}
	 * @param idx The pixel number; {@code idx == y * width + x}
	 * @param rgb The int representation of the new pixel color
	 */
	static native void writeRGB0(long ptr, int idx, int rgb);
	/**
	 * Read a pixel from a native framebuffer
	 * @param ptr A valid JNI pointer returned by {@link #openDevice0(String)}
	 * @param idx The pixel number; {@code idx == y * width + x}
	 * @return The int representation of the specified pixel color
	 */
	static native int readRGB0(long ptr, int idx);

	/**
	 * Try to open a linux framebuffer by path, throwing {@link IOException} if unable.
	 * @param fbdev The path to the framebuffer, such as {@code "/dev/fb1"}
	 * @return  A valid JNI pointer returned by {@link #openDevice0(String)}
	 * @throws IOException If the framebuffer could not be opened
	 */
	static long openDevice(String fbdev) throws IOException {
		long ptr = FrameBuffers.openDevice0(fbdev);

		if(ptr == FrameBuffers.ERR_NOT_SUPPORTED)
			throw new IOException("Linux framebuffers are not supported on this computer");
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
	
	/**
	 * Return whether the argument is a valid color depth ({@code 8}, {@code 16}, or {@code 24})
	 * @param bpp The color depth to test
	 * @return {@code true} if the color depth is valid, e.g. is one of {@code 8}, {@code 16}, or {@code 24}
	 */
	static boolean isValidColorDepth(int bpp) {
		return bpp == 8 || bpp == 16 || bpp == 24;
	}

	/*
	 * Can't instantiate this class
	 */
	private FrameBuffers() {
	}

	static {
		// load the JNI
		NarSystem.loadLibrary();
	}
}
