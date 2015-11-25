package org.tw.pi.framebuffer;

import java.awt.image.DataBuffer;
import java.io.Closeable;
import java.io.IOException;

/**
 * {@link DataBuffer} for use in a {@link FrameBufferedImage} that is usually
 * backed by a linux framebuffer device.  May also be instantiated using a
 * "dummy" framebuffer, just an array of ints.
 * @author Robin Kirkman
 *
 */
public class FrameBuffer extends DataBuffer implements Closeable {
	/**
	 * Default width of a dummy framebuffer
	 */
	public static final int DEFAULT_DUMMY_WIDTH = 320;
	/**
	 * Default height of a dummy framebuffer
	 */
	public static final int DEFAULT_DUMMY_HEIGHT = 240;
	/**
	 * Default color depth of a dummy framebuffer
	 */
	public static final int DEFAULT_COLOR_DEPTH = 24;
	
	/**
	 * Pointer to a {@code struct FrameBufferData} from the JNI layer.
	 */
	private final long ptr;
	/**
	 * Width of the frame buffer
	 */
	private final int w;
	/**
	 * Height of the frame buffer
	 */
	private final int h;
	/**
	 * Color depth of the frame buffer
	 */
	private final int bpp;

	/**
	 * {@code int} array used to hold data for a dummy buffer. is {@code null} if
	 * this {@link FrameBuffer} is backed by an actual framebuffer device.
	 */
	private final int[] dummy;
	
	/**
	 * Has this {@link FrameBuffer} been closed?
	 * @see #close()
	 */
	private boolean closed;

	/**
	 * Create a new dummy {@link FrameBuffer} with the default width, height, and color depth.
	 * @see #DEFAULT_DUMMY_WIDTH
	 * @see #DEFAULT_DUMMY_HEIGHT
	 * @see #DEFAULT_COLOR_DEPTH
	 */
	public FrameBuffer() {
		this(FrameBuffers.DUMMY);
	}
	
	/**
	 * Open a framebuffer with the argument device path, such as {@code "/dev/fb1"}.
	 * @param fbdev The path to the framebuffer device
	 * @throws IOException If the framebuffer could not be opened, such as on a non-linux system,
	 * or if the current user lacks sufficient privileges.
	 */
	public FrameBuffer(String fbdev) throws IOException {
		this(FrameBuffers.openDevice(fbdev));
	}

	/**
	 * Create a new dummy {@link FrameBuffer} with the specified width, height, and color depth.
	 * @param w The width
	 * @param h The height
	 * @param bpp The color depth: {@code 8}, {@code 16}, or {@code 24}
	 */
	public FrameBuffer(int w, int h, int bpp) {
		this(FrameBuffers.DUMMY, w, h, bpp);
	}

	/**
	 * Create a new dummy {@link FrameBuffer} with the specified width and height, and
	 * a default color depth.
	 * @param w The width
	 * @param h The height
	 * @see #DEFAULT_COLOR_DEPTH
	 */
	public FrameBuffer(int w, int h) {
		this(FrameBuffers.DUMMY, w, h, DEFAULT_COLOR_DEPTH);
	}

	/**
	 * Create a {@link FrameBuffer} by wrapping a {@code struct FrameBufferData*} returned
	 * by {@link FrameBuffers#openDevice0(String)}
	 * @param ptr The pointer from the JNI, or {@link FrameBuffers#DUMMY} for a default dummy {@link FrameBuffer}
	 */
	private FrameBuffer(long ptr) {
		this(
				ptr,
				(ptr == FrameBuffers.DUMMY ? DEFAULT_DUMMY_WIDTH : FrameBuffers.getDeviceWidth0(ptr)), 
				(ptr == FrameBuffers.DUMMY ? DEFAULT_DUMMY_HEIGHT : FrameBuffers.getDeviceHeight0(ptr)),
				(ptr == FrameBuffers.DUMMY ? DEFAULT_COLOR_DEPTH : FrameBuffers.getDeviceBitsPerPixel0(ptr)));
	}

	/**
	 * Create a {@link FrameBuffer} wrapping a {@code struct FrameBufferData*} returned
	 * by {@link FrameBuffers#openDevice0(String)}, with supplied width, height, and
	 * color depth.
	 * @param ptr The pointer from the JNI, or {@link FrameBuffers#DUMMY} for a dummy {@link FrameBuffer}
	 * @param w The width
	 * @param h The height
	 * @param bpp The color depth: {@code 8}, {@code 16}, or {@code 24}
	 */
	private FrameBuffer(long ptr, int w, int h, int bpp) {
		super(DataBuffer.TYPE_INT, w * h);
		if(!FrameBuffers.isValidColorDepth(bpp))
			throw new IllegalArgumentException("Illegal color depth: " + bpp);
		this.ptr = ptr;
		this.w = w;
		this.h = h;
		this.bpp = bpp;
		if(ptr == FrameBuffers.DUMMY) {
			this.dummy = new int[w * h];
		} else {
			this.dummy = null;
		}
		this.closed = false;
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.image.DataBuffer#getElem(int, int)
	 * 
	 * Return an int taken from the framebuffer device, or the dummy array for a dummy FrameBuffer
	 */
	@Override
	public int getElem(int bank, int i) {
		if(closed) // cannot get if closed
			throw new IllegalStateException();
		int val;
		if(ptr == FrameBuffers.DUMMY)
			val = dummy[i];
		else
			val = FrameBuffers.readRGB0(ptr, i);
		return val & 0x00FFFFFF; // ensure 24bit unsigned int
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.image.DataBuffer#setElem(int, int, int)
	 * 
	 * Set an int in the framebuffer device, or the dummy array for a dummy FrameBuffer
	 */
	@Override
	public void setElem(int bank, int i, int val) {
		if(closed) // cannot set if closed
			throw new IllegalStateException();
		val = val & 0x00FFFFFF; // ensure 24bit unsigned int
		if(ptr == FrameBuffers.DUMMY)
			dummy[i] = val;
		else
			FrameBuffers.writeRGB0(ptr, i, val);
	}

	/**
	 * Close this {@link FrameBuffer}, releasing any JNI resources (such as file
	 * handles or memory maps) for a non-dummy {@link FrameBuffer}.<p>
	 * 
	 * Subsequent calls to {@link #close()} have no effect.
	 */
	public void close() {
		if(!closed) {
			if(ptr != FrameBuffers.DUMMY)
				FrameBuffers.closeDevice0(ptr);
			closed = true;
		}
	}

	/**
	 * Returns the width of this {@link FrameBuffer}
	 * @return The width in pixels
	 */
	public int getWidth() {
		return w;
	}

	/**
	 * Returns the height of this {@link FrameBuffer}
	 * @return The height in pixels
	 */
	public int getHeight() {
		return h;
	}
	
	/**
	 * Returns the color depth of this {@link FrameBuffer}
	 * @return The color depth, in bits
	 */
	public int getColorDepth() {
		return bpp;
	}
}
