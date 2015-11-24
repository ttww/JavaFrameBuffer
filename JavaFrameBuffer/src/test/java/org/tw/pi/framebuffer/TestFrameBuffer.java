/*
 *	This file is the JNI Java part of a Raspberry Pi FrameBuffer project.
 *
 *	Created 2013 by Thomas Welsch (ttww@gmx.de).
 *
 *	Do whatever you want to do with it :-)
 *
 **/
package org.tw.pi.framebuffer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;


/**
 * Simple test file class for demonstrate the FrameBuffer class.
 */
public class TestFrameBuffer {

	private FrameBuffer fb;

	// -----------------------------------------------------------------------------------------------------------------

	public TestFrameBuffer(String deviceName) {
		fb = new FrameBuffer(deviceName);
	}

	// -----------------------------------------------------------------------------------------------------------------

	private void startTests() {

		new Thread("Test") {
			@Override
			public void run() {
				BufferedImage img = fb.getScreen();

				int w = img.getWidth();
				int h = img.getHeight();

				Graphics2D g = img.createGraphics();

				// RenderingHints.VALUE_ANTIALIAS_ON must before rotate !
				// Rotated font drawing behaves strange without that....
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

				g.setColor(Color.LIGHT_GRAY);
				g.fillRect(0, 0, w, h);

				g.setColor(Color.WHITE);
				g.drawString("Hello world !", 22, 45);

				int y = 17;
				g.setColor(Color.RED);
				g.fillRect(0, y, 20,20);
				y += 21;

				g.setColor(Color.GREEN);
				g.fillRect(0, y, 20,20);
				y += 21;

				g.setColor(Color.BLUE);
				g.fillRect(0, y, 20,20);
				y += 21;

				AffineTransform st = g.getTransform();
				g.translate(w/2, h/2+5);

				AffineTransform stt = g.getTransform();

				for (int i=0; i<360; i += 4) {

					g.rotate(Math.toRadians(i));

					g.setColor(Color.WHITE);
					g.drawString("Nice !!!", 0,0);

					try {
						sleep(150);
					} catch (InterruptedException e) {
						return;
					}

					g.setColor(Color.LIGHT_GRAY);
					g.drawString("Nice !!!", 0,0);

					g.setTransform(stt);
//					g.rotate(Math.toRadians(-i));
				}
				g.setTransform(st);


				g.setFont(new Font("Serif", Font.BOLD, 30));
				Color c1 = new Color(0, 0, 0, 0);
				Color c2 = new Color(0, 0, 0, 100);
				GradientPaint gradient = new GradientPaint(10, 8, c1, 10, 40, c2, true);

				g.setColor(Color.GREEN);
				g.fillRect(0, 0, w, h);
				g.setColor(Color.BLACK);
				g.setPaint(gradient);
				g.fillRoundRect(100, 100, 200, 50, 25, 25);
				g.setPaint(Color.BLACK);
				g.drawRoundRect(100, 100, 200, 50, 25, 25);
				g.drawString("Hello World!", 118, 135);

				try {
					sleep(2000);
				} catch (InterruptedException e) {
					return;
				}


				Random r = new Random();

				while (true) {
					int x1 = r.nextInt(w);
					int x2 = r.nextInt(w);
					int y1 = r.nextInt(h);
					int y2 = r.nextInt(h);

					g.setColor(new Color(r.nextInt(255),r.nextInt(255),r.nextInt(255)));
					g.drawLine(x1, y1, x2, y2);
				}
			}
		}.start();
	}

	// -----------------------------------------------------------------------------------------------------------------

	public static void main(String[] args) {

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
//				TestFrameBuffer mt = new TestFrameBuffer("/dev/fb1");
				TestFrameBuffer mt = new TestFrameBuffer("dummy_200x330");

				if (true) {
					JFrame f = new JFrame("Frame Buffer Test");
					f.setSize(400, 400);
					f.setLocation(300,200);
					f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					f.getContentPane().add(BorderLayout.CENTER, mt.fb.getScreenPanel());
					f.setVisible(true);
				}

				mt.startTests();
			}
		});
	}

}	// of TestFrameBuffer
