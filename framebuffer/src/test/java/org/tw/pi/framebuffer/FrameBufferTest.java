package org.tw.pi.framebuffer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.font.LineMetrics;
import java.util.Calendar;

import org.junit.Test;

public class FrameBufferTest {
	@Test
	public void testLoad() throws Exception {
		Class.forName(FrameBuffers.class.getName());
	}
	
	private static Point project(Point center, double value, double max, int length) {
		double radians = Math.PI / 2 - (value / (double) max) * (2 * Math.PI);
		Point p = new Point(center.x, center.y);
		p.x += length * Math.cos(radians);
		p.y -= length * Math.sin(radians);
		return p;
	}
	
	private static Dimension bounds(FontMetrics fm, String s) {
		Dimension d = new Dimension();
		d.height += fm.getAscent();
		d.width += fm.stringWidth(s);
		return d;
	}
	
	public static void main(String[] args) throws Exception {
		FrameBufferedImage fb = new FrameBufferedImage(args[0]);
		try {
			Graphics2D g = (Graphics2D) fb.getGraphics();
			Dimension dim = new Dimension(fb.getWidth(), fb.getHeight());
			g.setColor(Color.BLACK);
			g.fillRect(0, 0, dim.width, dim.height);

			Point center = new Point(dim.width / 2, dim.height / 2);
			int radius = Math.min(dim.width, dim.height) / 2 - 3;
			
			g.setStroke(new BasicStroke(2f));
			
			Calendar c = Calendar.getInstance();
			
			g.setColor(Color.RED);
			g.fillOval(center.x - radius, center.y - radius, radius * 2, radius * 2);
			
			g.setColor(Color.WHITE);
			g.drawOval(center.x - radius, center.y - radius, radius * 2, radius * 2);
			
			for(int i = 0; i < 12; i++) {
				Point p1 = project(center, i, 12, radius * 8 / 9);
				Point p2 = project(center, i, 12, radius * 19 / 20);
				g.drawLine(p1.x, p1.y, p2.x, p2.y);
			}
			
			g.setFont(new Font("Monospace", Font.BOLD, 12));
			FontMetrics fm = g.getFontMetrics();
			
			while(true) {
				c.setTimeInMillis(System.currentTimeMillis());

				double ms = c.get(Calendar.MILLISECOND);
				double second = c.get(Calendar.SECOND) + ms / 1000;
				double minute = c.get(Calendar.MINUTE) + second / 60;
				double hour = c.get(Calendar.HOUR) + minute / 60;

				if(((int) hour) == 0)
					hour += 12;
				
				Point p;
				Dimension d;
				
				g.setColor(Color.WHITE);
		
				p = project(center, hour, 12, radius * 1 / 2 - 12);
				g.drawLine(center.x, center.y, p.x, p.y);
				d = bounds(fm, String.valueOf((int) hour));
				p = project(center, hour, 12, radius * 1 / 2);
				g.drawString(String.valueOf((int) hour), p.x - d.width / 2, p.y + d.height / 2);
				
				
				p = project(center, minute, 60, radius * 2 / 3 - 12);
				g.drawLine(center.x, center.y, p.x, p.y);
				d = bounds(fm, String.valueOf((int) minute));
				p = project(center, minute, 60, radius * 2 / 3);
				g.drawString(String.valueOf((int) minute), p.x - d.width / 2, p.y + d.height / 2);
				
				p = project(center, second, 60, radius * 3 / 4 - 12);
				g.drawLine(center.x, center.y, p.x, p.y);
				d = bounds(fm, String.valueOf((int) second));
				p = project(center, second, 60, radius * 3 / 4);
				g.drawString(String.valueOf((int) second), p.x - d.width / 2, p.y + d.height / 2);

				
				Thread.sleep(125);

				g.setColor(Color.RED);

				p = project(center, hour, 12, radius * 1 / 2 - 12);
				g.drawLine(center.x, center.y, p.x, p.y);
				d = bounds(fm, String.valueOf((int) hour));
				p = project(center, hour, 12, radius * 1 / 2);
				g.drawString(String.valueOf((int) hour), p.x - d.width / 2, p.y + d.height / 2);
				
				
				p = project(center, minute, 60, radius * 2 / 3 - 12);
				g.drawLine(center.x, center.y, p.x, p.y);
				d = bounds(fm, String.valueOf((int) minute));
				p = project(center, minute, 60, radius * 2 / 3);
				g.drawString(String.valueOf((int) minute), p.x - d.width / 2, p.y + d.height / 2);
				
				p = project(center, second, 60, radius * 3 / 4 - 12);
				g.drawLine(center.x, center.y, p.x, p.y);
				d = bounds(fm, String.valueOf((int) second));
				p = project(center, second, 60, radius * 3 / 4);
				g.drawString(String.valueOf((int) second), p.x - d.width / 2, p.y + d.height / 2);
				
			}
		} finally {
			fb.close();
		}
	}
}
