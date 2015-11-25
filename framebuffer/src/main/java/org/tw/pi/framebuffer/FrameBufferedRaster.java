package org.tw.pi.framebuffer;

import java.awt.Point;
import java.awt.image.DataBuffer;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.Closeable;

/**
 * {@link WritableRaster} that uses a {@link FrameBuffer} as its {@link DataBuffer}.
 * @author Robin Kirkman
 *
 */
public class FrameBufferedRaster extends WritableRaster implements Closeable {

	/**
	 * The {@link FrameBuffer} backing this {@link FrameBufferedRaster}
	 */
	final private FrameBuffer fb;
	
	/**
	 * Create a new {@link FrameBufferedRaster} using RGB color endian-ness
	 * @param fb The {@link FrameBuffer} to use
	 * @see ColorEndian#RGB
	 */
	public FrameBufferedRaster(FrameBuffer fb) {
		this(ColorEndian.RGB, fb);
	}
	
	/**
	 * Create a new {@link FrameBufferedRaster}
	 * @param ce The {@link ColorEndian} to use: {@link ColorEndian#RGB} or {@link ColorEndian#BGR}
	 * @param fb The {@link FrameBuffer} to use
	 */
	public FrameBufferedRaster(ColorEndian ce, FrameBuffer fb) {
		this(fb, ce.createSampleModel(fb.getWidth(), fb.getHeight(), fb.getColorDepth()));
	}

	/**
	 * Create a new {@link FrameBufferedRaster} using a {@link FrameBuffer} and
	 * a {@link SampleModel} returned by {@link ColorEndian#createSampleModel(int, int, int)}
	 * @param fb The {@link FrameBuffer} to use
	 * @param sampleModel The {@link SampleModel} to use
	 */
	private FrameBufferedRaster(FrameBuffer fb, SampleModel sampleModel) {
		super(sampleModel, fb, new Point(0,0));
		this.fb = fb;
	}
	
	/**
	 * Returns the {@link FrameBuffer} used by this {@link FrameBufferedRaster}
	 * @return The {@link FrameBuffer}
	 */
	public FrameBuffer getFrameBuffer() {
		return fb;
	}
	
	@Override
	public void close() {
		fb.close();
	}
}
