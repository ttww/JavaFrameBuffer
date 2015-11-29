
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

/*
 * struct to keep track of an open linux framebuffer
 */
static struct FrameBufferData {
	/*
	 * The path to the framebuffer, such as /dev/fb1
	 */
	char *deviceName;

	/*
	 * file descriptor for the framebuffer
	 */
	int fbfd;

	/*
	 * Width of the framebuffer
	 */
	int width;
	/*
	 * Height of the framebuffer
	 */
	int height;
	/*
	 * Color depth of the framebuffer
	 */
	int bpp;

	/*
	 * Size of the memory-mapped framebuffer device
	 */
	long int screensize;

	char *fbp;
};

/*
 * Open a linux framebuffer, returning a pointer to struct FrameBufferData,
 * or an error code defined in FrameBuffers.java
 */
JNIEXPORT jlong JNICALL Java_org_tw_pi_framebuffer_FrameBuffers_openDevice0(
		JNIEnv *env, jobject obj, jstring device) {

	jboolean isCopy;

	struct FrameBufferData *di;

#ifndef __linux
	// not supported on not linux
	return org_tw_pi_framebuffer_FrameBuffers_ERR_NOT_SUPPORTED;
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
		free(di->deviceName);
		return org_tw_pi_framebuffer_FrameBuffers_ERR_OPEN;
	}

	// Get fixed screen information
	if (ioctl(di->fbfd, FBIOGET_FSCREENINFO, &finfo)) {
		close(di->fbfd);
		free(di->deviceName);
		return org_tw_pi_framebuffer_FrameBuffers_ERR_FIXED;
	}

	// Get variable screen information
	if (ioctl(di->fbfd, FBIOGET_VSCREENINFO, &vinfo)) {
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

	// map framebuffer to user memory
	di->screensize = finfo.smem_len;

	di->fbp = (char*) mmap(0, di->screensize, PROT_READ | PROT_WRITE, MAP_SHARED, di->fbfd, 0);

	if ((int) di->fbp == -1) {
		close(di->fbfd);
		free(di->deviceName);
		return org_tw_pi_framebuffer_FrameBuffers_ERR_MMAP;
	}

	return (jlong) (intptr_t) di;
#endif
}

/*
 * Close a framebuffer tracked by a pointer to struct FrameBufferData
 */
JNIEXPORT void JNICALL Java_org_tw_pi_framebuffer_FrameBuffers_closeDevice0(
		JNIEnv *env, jobject obj, jlong jdi) {

	struct FrameBufferData *di = (struct FrameBufferData *) (intptr_t) jdi;

	free(di->deviceName);

	if (di->fbfd != 0) {
		munmap(di->fbp, di->screensize);
		close(di->fbfd);
	}

	memset(di, 0, sizeof(*di));
}

/*
 * Return the tracked framebuffer width by a pointer to struct FrameBufferData
 */
JNIEXPORT jint JNICALL Java_org_tw_pi_framebuffer_FrameBuffers_getDeviceWidth0(
		JNIEnv *env, jobject obj, jlong jdi) {

	struct FrameBufferData *di = (struct FrameBufferData *) (intptr_t) jdi;

	return di->width;
}

/*
 * Return the tracked framebuffer height by a pointer to struct FrameBufferData
 */
JNIEXPORT jint JNICALL Java_org_tw_pi_framebuffer_FrameBuffers_getDeviceHeight0(
		JNIEnv *env, jobject obj, jlong jdi) {

	struct FrameBufferData *di = (struct FrameBufferData *) (intptr_t) jdi;

	return di->height;
}

/*
 * Return the tracked framebuffer color depth by a pointer to struct FrameBufferData
 */
JNIEXPORT jint JNICALL Java_org_tw_pi_framebuffer_FrameBuffers_getDeviceBitsPerPixel0(
		JNIEnv *env, jobject obj, jlong jdi) {

	struct FrameBufferData *di = (struct FrameBufferData *) (intptr_t) jdi;

	return di->bpp;
}

/*
 * Write a pixel to a framebuffer
 */
JNIEXPORT void JNICALL Java_org_tw_pi_framebuffer_FrameBuffers_writeRGB0
(JNIEnv *env, jclass clazz, jlong ptr, jint idx, jint rgb) {
	struct FrameBufferData	*di = (struct FrameBufferData *) (intptr_t) ptr;
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
	while(width > 0) {
		*p = (unsigned char)(0xFF & rgb);
		rgb = rgb >> 8;
		width--;
		p++;
	}
}

/*
 * Read a pixel from a framebuffer
 */
JNIEXPORT jint JNICALL Java_org_tw_pi_framebuffer_FrameBuffers_readRGB0
(JNIEnv *env, jclass clazz, jlong ptr, jint idx) {
	struct FrameBufferData	*di = (struct FrameBufferData *) (intptr_t) ptr;
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
	p += width - 1;
	while(width > 0) {
		rgb = (rgb << 8) + *p;
		width--;
		p--;
	}

	return rgb;
}
