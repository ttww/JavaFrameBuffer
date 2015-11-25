# org.tw.pi:framebuffer:0.1.0-SNAPSHOT

Access to Linux frame buffer devices from Java. E.g. for driving LCD SPI displays on the Raspberry Pi

Originally based off code from [ttww/JavaFrameBuffer](https://github.com/ttww/JavaFrameBuffer).
Modified to build with Apache Maven, then API rewritten to provide direct-write access
to linux framebuffers as java.awt.image.BuffferedImage
 