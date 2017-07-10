@echo off
SET jniResult=FrameBufferJNI.dll

SET jniJdkHeader=$JAVA_HOME\include
SET jniSysHeader=$JAVA_HOME\include\win32

del "%jniResult%"

echo "javac"
mkdir bin
javac -d bin src\main\java\org\tw\pi\framebuffer\FrameBuffer.java

echo "javah"
javah -d src\main\c -classpath bin org.tw.pi.framebuffer.FrameBuffer
del src\main\c\org_tw_pi_framebuffer_FrameBuffer_AutoUpdateThread.h
del src\main\c\org_tw_pi_framebuffer_FrameBuffer_ManualRepaintThread.h
del src\main\c\org_tw_pi_framebuffer_FrameBuffer_ScreenPanel.h


echo "gcc"
gcc -Wall -O2 -o "%jniResult%" -shared -Wl,--kill-at -I "%jniJdkHeader%"  -I "%jniSysHeader%" -I src\main\c src\main\c\FrameBuffer.c 

echo ""
dir "%jniResult%"
echo "done"

