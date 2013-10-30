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
 *	For testing purpose a dummy device is supported (via the devicename "dummy_160x128" instead of "/dev/fb1").
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

#include "org_tw_pi_framebuffer_FrameBuffer.h"

// ---------------------------------------------------------------------------------------------------------------------
// Handle structur from Java:
// ---------------------------------------------------------------------------------------------------------------------
struct deviceInfo {
	char *deviceName;				// Device-Name from Java ("/dev/fb1" or "dummy_240x180")...
	int fbfd;						// File descriptor, 0 for dummy devices

	int width;
	int height;
	int bpp;						// BitsPerPixel, 0 for dummy devices

	long int screensize;			// Buffer size in bytes

	char *fbp;						// MemoryMapped buffer

	unsigned int *currentScreen;	// Last screen
};

// ---------------------------------------------------------------------------------------------------------------------
//	long	openDevice(String device);
// ---------------------------------------------------------------------------------------------------------------------
JNIEXPORT jlong JNICALL Java_org_tw_pi_framebuffer_FrameBuffer_openDevice(
		JNIEnv *env, jobject obj, jstring device) {

	jboolean isCopy;

	struct deviceInfo *di;

	di = malloc(sizeof(*di));
	memset(di, 0, sizeof(*di));

	const char *s = (*env)->GetStringUTFChars(env, device, &isCopy);
	di->deviceName = strdup(s);
	if (isCopy)
		(*env)->ReleaseStringUTFChars(env, device, s);

	// Open the file for reading and writing
	if (*di->deviceName == '/') {

#ifndef __linux
		printf("Error: Framebuffer only under linux, use dummy device (dummy_220x440) instead %s\n",di->deviceName);
		return (1);
#else

		struct fb_var_screeninfo vinfo;
		struct fb_fix_screeninfo finfo;

		di->fbfd = open(di->deviceName, O_RDWR);
		if (!di->fbfd) {
			printf("Error: cannot open framebuffer device. %s\n",
					di->deviceName);
			return (1);
		}
		printf("The framebuffer device %s was opened successfully.\n",
				di->deviceName);

		// Get fixed screen information
		if (ioctl(di->fbfd, FBIOGET_FSCREENINFO, &finfo)) {
			printf("Error reading fixed information.\n");
			return (2);
		}

		// Get variable screen information
		if (ioctl(di->fbfd, FBIOGET_VSCREENINFO, &vinfo)) {
			printf("Error reading variable information.\n");
			return (3);
		}

		di->width = vinfo.xres;
		di->height = vinfo.yres;
		di->bpp = vinfo.bits_per_pixel;
		di->currentScreen = malloc(vinfo.xres * vinfo.yres * sizeof(int));

		printf("%dx%d, %d bpp  %ld bytes\n", vinfo.xres, vinfo.yres,
				vinfo.bits_per_pixel, (long) finfo.smem_len);

		// map framebuffer to user memory
		di->screensize = finfo.smem_len;

		di->fbp = (char*) mmap(0, di->screensize, PROT_READ | PROT_WRITE,
				MAP_SHARED, di->fbfd, 0);

		if ((int) di->fbp == -1) {
			printf("Failed to mmap.\n");
			return (4);
		}
#endif
	} else {
		// Parse dummy_123x343
		sscanf(di->deviceName, "dummy_%dx%d", &di->width, &di->height);
		di->bpp = 0;
		di->currentScreen = malloc(di->width * di->height * sizeof(int));
	}
	return (jlong) (intptr_t) di;
}

// ---------------------------------------------------------------------------------------------------------------------
//	void		closeDevice(long di);
// ---------------------------------------------------------------------------------------------------------------------
JNIEXPORT void JNICALL Java_org_tw_pi_framebuffer_FrameBuffer_closeDevice(
		JNIEnv *env, jobject obj, jlong jdi) {

	struct deviceInfo *di = (struct deviceInfo *) (intptr_t) jdi;

	free(di->deviceName);
	free(di->currentScreen);

	if (di->fbfd != 0) {
		munmap(di->fbp, di->screensize);
		close(di->fbfd);
	}

	memset(di, 0, sizeof(*di)); // :-)
}

// ---------------------------------------------------------------------------------------------------------------------
//	int		getDeviceWidth(long di);
// ---------------------------------------------------------------------------------------------------------------------
JNIEXPORT jint JNICALL Java_org_tw_pi_framebuffer_FrameBuffer_getDeviceWidth(
		JNIEnv *env, jobject obj, jlong jdi) {

	struct deviceInfo *di = (struct deviceInfo *) (intptr_t) jdi;

	return di->width;
}

// ---------------------------------------------------------------------------------------------------------------------
//	int		getDeviceHeight(long di);
// ---------------------------------------------------------------------------------------------------------------------
JNIEXPORT jint JNICALL Java_org_tw_pi_framebuffer_FrameBuffer_getDeviceHeight(
		JNIEnv *env, jobject obj, jlong jdi) {

	struct deviceInfo *di = (struct deviceInfo *) (intptr_t) jdi;

	return di->height;
}

// ---------------------------------------------------------------------------------------------------------------------
//	int		getDeviceBitsPerPixel(long di);
// ---------------------------------------------------------------------------------------------------------------------
JNIEXPORT jint JNICALL Java_org_tw_pi_framebuffer_FrameBuffer_getDeviceBitsPerPixel(
		JNIEnv *env, jobject obj, jlong jdi) {

	struct deviceInfo *di = (struct deviceInfo *) (intptr_t) jdi;

	return di->bpp;
}

// ---------------------------------------------------------------------------------------------------------------------
//	boolean	updateDeviceBuffer(long di,int[] buffer);
// ---------------------------------------------------------------------------------------------------------------------
JNIEXPORT jboolean JNICALL Java_org_tw_pi_framebuffer_FrameBuffer_updateDeviceBuffer(
		JNIEnv *env, jobject obj, jlong jdi, jintArray buf) {

	struct deviceInfo	*di = (struct deviceInfo *) (intptr_t) jdi;
	int					i;
	jsize				len = (*env)->GetArrayLength(env, buf);
	unsigned int		*current = di->currentScreen;
	int					updated = 0;


// See http://docs.oracle.com/javase/1.5.0/docs/guide/jni/spec/functions.html
#define USE_CRITICAL		// Avoid copy, but blocks gc....
#ifdef USE_CRITICAL
	jint			*body = (*env)->GetPrimitiveArrayCritical(env, buf, 0);
#else
	jboolean		isCopy;
	jint			*body = (*env)->GetIntArrayElements(env, buf, &isCopy);
#endif

	switch (di->bpp) {
	case 0: {
		// Dummy Device
		for (i = 0; i < len; i++) {
			unsigned int u = body[i];

			if (current[i] == u)
				continue;
			updated = 1;
			current[i] = u;
		}
	}
	break;
	case 16: {
		// Comment from:
		//		http://raspberrycompote.blogspot.de/2013/03/low-level-graphics-on-raspberry-pi-part_8.html
		//
		// The red value has 5 bits, so can be in the range 0-31, therefore divide the original 0-255
		// value by 8. It is stored in the first 5 bits, so multiply by 2048 or shift 11 bits left.
		// The green has 6 bits, so can be in the range 0-63, divide by 4, and multiply by 32 or shift
		// 5 bits left. Finally the blue has 5 bits and is stored at the last bits, so no need to move.

		unsigned short *p = (unsigned short *) di->fbp;

		for (i = 0; i < len; i++) {
			unsigned int u = body[i];

			if (current[i] == u) continue;

			updated = 1;
			current[i] = u;

			unsigned char r = (u >> 16) & 0x0ff;
			unsigned char g = (u >> 8) & 0x0ff;
			unsigned char b = (u) & 0x0ff;

			//printf("%5d:   %#x  %3d  %3d  %3d  %3d\n",i,u,a,r,g,b);
			u = ((r / 8) << 11) + ((g / 4) << 5) + (b / 8);

			p[i] = u;
		}
	}
		break;
		/*
		 case 24:
		 {
		 // untested...
		 for (i=0; i<len; i++) {
		 unsigned int u = body[i];

		 if (current[i] == u) continue;
		 current[i] = u;

		 unsigned char r = (u >>16) & 0x0ff;
		 unsigned char g = (u >> 8) & 0x0ff;
		 unsigned char b = (u) & 0x0ff;

		 unsigned char *p = ((unsigned char *) di->fbp) + i + i + i;
		 *p++ = r;
		 *p++ = g;
		 *p = b;
		 }
		 }
		 break;
		 */
	default:
		fprintf(stderr, "FrameBuffer depth %d not supported, use 16 !\n",
				di->bpp);
	}

#ifdef USE_CRITICAL
	(*env)->ReleasePrimitiveArrayCritical(env, buf, body, 0);
#else
	if (isCopy) (*env)->ReleaseIntArrayElements(env, buf, body, 0);
#endif

	return updated;
}

