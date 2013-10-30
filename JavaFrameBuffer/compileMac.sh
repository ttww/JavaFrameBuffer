#!/bin/sh

jniResult=libFrameBufferJNI.jnilib

jniJdkHeader=/Library/Java/JavaVirtualMachines/jdk1.7.0_40.jdk/Contents/Home/include
jniSysHeader=/Library/Java/JavaVirtualMachines/jdk1.7.0_40.jdk/Contents/Home/include/darwin

rm "$jniResult" 2>/dev/null

echo "javah"
javah -d src/main/c  -classpath bin org.tw.pi.framebuffer.FrameBuffer
rm src/main/c/org_tw_pi_framebuffer_FrameBuffer_ScreenPanel.h
rm src/main/c/org_tw_pi_framebuffer_FrameBuffer_UpdateThread.h


echo "gcc"
gcc -Wall -O3 -o "$jniResult" -dynamiclib -I "$jniJdkHeader"  -I "$jniSysHeader" -I src/main/c src/main/c/FrameBuffer.c 

echo
ls -l "$jniResult"
echo "done"

