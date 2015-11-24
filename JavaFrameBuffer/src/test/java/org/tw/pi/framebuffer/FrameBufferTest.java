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
		FrameBuffer fb = new FrameBuffer(args[0]);
		BufferedImage buf = fb.getBufferedImage();
		BufferedImage saved = new BufferedImage(buf.getWidth(), buf.getHeight(), buf.getType());
		fb.read();
		saved.getGraphics().drawImage(buf, 0, 0, null);
		try {
			for(Color c = Color.WHITE; !c.equals(Color.BLACK); c = c.darker()) {
				Graphics g = buf.getGraphics();
				g.setColor(c);
				g.fillRect(0, 0, buf.getWidth(), buf.getHeight());
				fb.write();
				Thread.sleep(100);
			}
			buf.getGraphics().drawImage(saved, 0, 0, null);
			fb.write();
		} finally {
			fb.close();
		}
	}
}
