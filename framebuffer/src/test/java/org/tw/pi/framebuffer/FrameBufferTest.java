package org.tw.pi.framebuffer;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import org.junit.Test;

public class FrameBufferTest {
	@Test
	public void testLoad() throws Exception {
		Class.forName(FrameBuffer.class.getName());
	}
	
	public static void main(String[] args) throws Exception {
		FrameBufferImage fb = new FrameBufferImage(args[0]);
		try {
			Graphics g = fb.getGraphics();
			for(int i = 0; i < Math.min(fb.getWidth(), fb.getHeight()) / 2; i++) {
				g.setColor(new Color((int)(0xFFFFFF * Math.random())));
				g.drawRect(i, i, fb.getWidth() - 2*i, fb.getHeight() - 2*i);
			}
		} finally {
			fb.close();
		}
	}
}
