/*
 *	This file is the JNI C part of a Raspberry Pi FrameBuffer project.
 *
 *	Created 2013 by Thomas Welsch (ttww@gmx.de).
 *
 *	Do whatever you want to do with it :-)
 *
 *	This code transfers an Java BufferedImage ARGB data array to a FrameBuffer device
 *	(e.g. SPI-Displays like http://www.sainsmart.com/blog/ada/).
 *
 *	For testing purpose a previous device is supported (via the devicename "dummy_160x128" instead of "/dev/fb1").
 *
 **/

#include <unistd.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <fcntl.h>

#ifdef __linux
#include <linux/fb.h>
#include <sys/ioctl.h>
#endif

#include <sys/mman.h>

#include <jni.h>

#include "org_tw_pi_framebuffer_FrameBuffer.h"

struct deviceInfo {
	char *deviceName;				// Device-Name from Java ("/dev/fb1" or "dummy_240x180")...
	int fbfd;						// File descriptor, 0 for previous devices

	int width;
	int height;
	int bpp;						// BitsPerPixel, 0 for previous devices

	long int screensize;			// Buffer size in bytes

	char *fbp;						// MemoryMapped buffer
};

JNIEXPORT jlong JNICALL Java_org_tw_pi_framebuffer_FrameBuffer_openDevice(
		JNIEnv *env, jobject obj, jstring device) {

	jboolean isCopy;

	struct deviceInfo *di;

#ifndef __linux
	return org_tw_pi_framebuffer_FrameBuffer_DUMMY;
#else

	di = malloc(sizeof(*di));
	memset(di, 0, sizeof(*di));

	const char *s = (*env)->GetStringUTFChars(env, device, &isCopy);
	di->deviceName = strdup(s);
	if (isCopy)
		(*env)->ReleaseStringUTFChars(env, device, s);

	// Open the file for reading and writing
	struct fb_var_screeninfo vinfo;
	struct fb_fix_screeninfo finfo;

	di->fbfd = open(di->deviceName, O_RDWR);
	if (!di->fbfd) {
		//			printf("Error: cannot open framebuffer device. %s\n", di->deviceName);
		free(di->deviceName);
		return org_tw_pi_framebuffer_FrameBuffer_ERR_OPEN;
	}
	//		printf("The framebuffer device %s was opened successfully.\n", di->deviceName);

	// Get fixed screen information
	if (ioctl(di->fbfd, FBIOGET_FSCREENINFO, &finfo)) {
		//			printf("Error reading fixed information.\n");
		close(di->fbfd);
		free(di->deviceName);
		return org_tw_pi_framebuffer_FrameBuffer_ERR_FIXED;
	}

	// Get variable screen information
	if (ioctl(di->fbfd, FBIOGET_VSCREENINFO, &vinfo)) {
		//			printf("Error reading variable information.\n");
		close(di->fbfd);
		free(di->deviceName);
		return org_tw_pi_framebuffer_FrameBuffer_ERR_VARIABLE;
	}

	di->width = vinfo.xres;
	di->height = vinfo.yres;
	di->bpp = vinfo.bits_per_pixel;

	if(di->bpp != 16 && di->bpp != 24) {
		close(di->fbfd);
		free(di->deviceName);
		return org_tw_pi_framebuffer_FrameBuffer_ERR_BITS;
	}

	//		printf("%dx%d, %d bpp  %ld bytes\n", vinfo.xres, vinfo.yres, vinfo.bits_per_pixel, (long) finfo.smem_len);

	// map framebuffer to user memory
	di->screensize = finfo.smem_len;

	di->fbp = (char*) mmap(0, di->screensize, PROT_READ | PROT_WRITE,
			MAP_SHARED, di->fbfd, 0);

	if ((int) di->fbp == -1) {
		//			printf("Failed to mmap.\n");
		close(di->fbfd);
		free(di->deviceName);
		return org_tw_pi_framebuffer_FrameBuffer_ERR_MMAP;
	}

	return (jlong) (intptr_t) di;
#endif
}

JNIEXPORT void JNICALL Java_org_tw_pi_framebuffer_FrameBuffer_closeDevice(
		JNIEnv *env, jobject obj, jlong jdi) {

	struct deviceInfo *di = (struct deviceInfo *) (intptr_t) jdi;

	free(di->deviceName);

	if (di->fbfd != 0) {
		munmap(di->fbp, di->screensize);
		close(di->fbfd);
	}

	memset(di, 0, sizeof(*di)); // :-)
}

JNIEXPORT jint JNICALL Java_org_tw_pi_framebuffer_FrameBuffer_getDeviceWidth(
		JNIEnv *env, jobject obj, jlong jdi) {

	struct deviceInfo *di = (struct deviceInfo *) (intptr_t) jdi;

	return di->width;
}

JNIEXPORT jint JNICALL Java_org_tw_pi_framebuffer_FrameBuffer_getDeviceHeight(
		JNIEnv *env, jobject obj, jlong jdi) {

	struct deviceInfo *di = (struct deviceInfo *) (intptr_t) jdi;

	return di->height;
}

JNIEXPORT jint JNICALL Java_org_tw_pi_framebuffer_FrameBuffer_getDeviceBitsPerPixel(
		JNIEnv *env, jobject obj, jlong jdi) {

	struct deviceInfo *di = (struct deviceInfo *) (intptr_t) jdi;

	return di->bpp;
}

JNIEXPORT void JNICALL Java_org_tw_pi_framebuffer_FrameBuffer_writeRGB
(JNIEnv *env, jclass clazz, jlong ptr, jint idx, jint rgb) {
	struct deviceInfo	*di = (struct deviceInfo *) (intptr_t) ptr;
	if(di->bpp == 16) {
		unsigned short *p = (unsigned short *) di->fbp;
		unsigned char r = (rgb >> 16) & 0x0ff;
		unsigned char g = (rgb >> 8) & 0x0ff;
		unsigned char b = (rgb) & 0x0ff;

		p[idx] = ((r / 8) << 11) + ((g / 4) << 5) + (b / 8);
	} else if(di->bpp == 24) {
		unsigned char *p = (unsigned char *) di->fbp;
		unsigned char r = (unsigned char)(0xFF & (rgb >> 16));
		unsigned char g = (unsigned char)(0xFF & (rgb >> 8));
		unsigned char b = (unsigned char)(0xFF & rgb);

		p[3*idx] = r;
		p[3*idx + 1] = g;
		p[3*idx + 2] = b;
	}
}

JNIEXPORT jint JNICALL Java_org_tw_pi_framebuffer_FrameBuffer_readRGB
(JNIEnv *env, jclass clazz, jlong ptr, jint idx) {
	struct deviceInfo	*di = (struct deviceInfo *) (intptr_t) ptr;
	if(di->bpp == 16) {
		unsigned short *p = (unsigned short *) di->fbp;
		unsigned int r = 0xff & ((rgb >> 11) << 3);
		unsigned int g = 0xff & ((rgb >> 5) << 2);
		unsigned int b = 0xff & (rgb << 2);

		return (r << 16) + (g << 8) + b;
	} else if(di->bpp == 24) {
		unsigned char *p = (unsigned char *) di->fbp;
		unsigned int r = p[3*idx];
		unsigned int g = p[3*idx + 1];
		unsigned int b = p[3*idx + 2];

		return (r << 16) + (g << 8) + b;
	} else
		return -1;
}
