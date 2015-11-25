package org.tw.pi.framebuffer;

import java.awt.Color;
import java.awt.Graphics;
import org.junit.Test;

public class FrameBufferTest {
	@Test
	public void testLoad() throws Exception {
		Class.forName(FrameBuffers.class.getName());
	}
	
	public static void main(String[] args) throws Exception {
		FrameBufferImage fb = new FrameBufferImage(args[0]);
		try {
			Graphics g = fb.getGraphics();
			int m = 7;
			for(int i = 0; i < Math.min(fb.getWidth(), fb.getHeight()) / 2; i++) {
				g.setColor(new Color(m));
				g.drawRect(i, i, fb.getWidth() - 2*i, fb.getHeight() - 2*i);
				m <<= 1;
				m |= (m >>> 24);
			}
		} finally {
			fb.close();
		}
	}
}
