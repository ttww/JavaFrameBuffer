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

#include "org_tw_pi_framebuffer_FrameBuffers.h"

struct deviceInfo {
	char *deviceName;				// Device-Name from Java ("/dev/fb1" or "dummy_240x180")...
	int fbfd;						// File descriptor, 0 for previous devices

	int width;
	int height;
	int bpp;						// BitsPerPixel, 0 for previous devices

	long int screensize;			// Buffer size in bytes

	char *fbp;						// MemoryMapped buffer
};

JNIEXPORT jlong JNICALL Java_org_tw_pi_framebuffer_FrameBuffers_openDevice(
		JNIEnv *env, jobject obj, jstring device) {

	jboolean isCopy;

	struct deviceInfo *di;

#ifndef __linux
	return org_tw_pi_framebuffer_FrameBuffers_DUMMY;
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
		return org_tw_pi_framebuffer_FrameBuffers_ERR_OPEN;
	}
	//		printf("The framebuffer device %s was opened successfully.\n", di->deviceName);

	// Get fixed screen information
	if (ioctl(di->fbfd, FBIOGET_FSCREENINFO, &finfo)) {
		//			printf("Error reading fixed information.\n");
		close(di->fbfd);
		free(di->deviceName);
		return org_tw_pi_framebuffer_FrameBuffers_ERR_FIXED;
	}

	// Get variable screen information
	if (ioctl(di->fbfd, FBIOGET_VSCREENINFO, &vinfo)) {
		//			printf("Error reading variable information.\n");
		close(di->fbfd);
		free(di->deviceName);
		return org_tw_pi_framebuffer_FrameBuffers_ERR_VARIABLE;
	}

	di->width = vinfo.xres;
	di->height = vinfo.yres;
	di->bpp = vinfo.bits_per_pixel;

	if(di->bpp != 8 && di->bpp != 16 && di->bpp != 24) {
		close(di->fbfd);
		free(di->deviceName);
		return org_tw_pi_framebuffer_FrameBuffers_ERR_BITS;
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
		return org_tw_pi_framebuffer_FrameBuffers_ERR_MMAP;
	}

	return (jlong) (intptr_t) di;
#endif
}

JNIEXPORT void JNICALL Java_org_tw_pi_framebuffer_FrameBuffers_closeDevice(
		JNIEnv *env, jobject obj, jlong jdi) {

	struct deviceInfo *di = (struct deviceInfo *) (intptr_t) jdi;

	free(di->deviceName);

	if (di->fbfd != 0) {
		munmap(di->fbp, di->screensize);
		close(di->fbfd);
	}

	memset(di, 0, sizeof(*di)); // :-)
}

JNIEXPORT jint JNICALL Java_org_tw_pi_framebuffer_FrameBuffers_getDeviceWidth(
		JNIEnv *env, jobject obj, jlong jdi) {

	struct deviceInfo *di = (struct deviceInfo *) (intptr_t) jdi;

	return di->width;
}

JNIEXPORT jint JNICALL Java_org_tw_pi_framebuffer_FrameBuffers_getDeviceHeight(
		JNIEnv *env, jobject obj, jlong jdi) {

	struct deviceInfo *di = (struct deviceInfo *) (intptr_t) jdi;

	return di->height;
}

JNIEXPORT jint JNICALL Java_org_tw_pi_framebuffer_FrameBuffers_getDeviceBitsPerPixel(
		JNIEnv *env, jobject obj, jlong jdi) {

	struct deviceInfo *di = (struct deviceInfo *) (intptr_t) jdi;

	return di->bpp;
}

JNIEXPORT void JNICALL Java_org_tw_pi_framebuffer_FrameBuffers_writeRGB
(JNIEnv *env, jclass clazz, jlong ptr, jint idx, jint rgb) {
	struct deviceInfo	*di = (struct deviceInfo *) (intptr_t) ptr;
	unsigned char *p = (unsigned char *) di->fbp;
	unsigned int width;

	if(di->bpp == 8)
		width = 1;
	else if(di->bpp == 16)
		width = 2;
	else if(di->bpp == 24)
		width = 3;
	else
		return;

	p += (width * idx);
	p += (width - 1);
	while(width > 0) {
		*p = (unsigned char)(0xFF & rgb);
		rgb = rgb >> 8;
		width--;
		p--;
	}
}

JNIEXPORT jint JNICALL Java_org_tw_pi_framebuffer_FrameBuffers_readRGB
(JNIEnv *env, jclass clazz, jlong ptr, jint idx) {
	struct deviceInfo	*di = (struct deviceInfo *) (intptr_t) ptr;
	unsigned char *p = (unsigned char *) di->fbp;
	unsigned int width;

	if(di->bpp == 8)
		width = 1;
	else if(di->bpp == 16)
		width = 2;
	else if(di->bpp == 24)
		width = 3;
	else
		return -1;

	unsigned int rgb = 0;

	p += (width * idx);
	while(width > 0) {
		rgb = (rgb << 8) + *p;
		width--;
		p++;
	}

	return rgb;
}
