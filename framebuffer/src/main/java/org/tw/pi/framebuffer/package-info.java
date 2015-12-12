package org.tw.pi.framebuffer;
/**
 * Java classes for accessing a linux framebuffer device as a {@link java.awt.image.BufferedImage}.<p>
 * 
 * The framebuffer is updated immediately when the {@link java.awt.image.BufferedImage} is changed
 * by using a custom {@link java.awt.image.DataBuffer} subclass, {@link org.tw.pi.framebuffer.FrameBuffer},
 * which uses a JNI-accessed framebuffer device as its backing store.<p>
 * 
 * This package makes use of the Java AWT image classes, so anything a {@link java.awt.image.BufferedImage}
 * can do can be done with a framebuffer device.
 * 
 * @author Robin Kirkman
 */

