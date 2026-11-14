OpenIMAJ
pros
- Functions like a classic Java library, doesn't feel too alien.
cons
- bloated af
- Maintained by a small group at a university, so not many updates.

Video4J
pros
- Linux support!
- Leak-proof
- very lightweight
cons
- niche
- made *for* Linux, so it'll prob struggle on Mac and Windows

JAVAE2
pros
- cross-platform
- stable
- can convert file formats to-and-fro
cons
- slowwwwwwww
- coarse control (whatever that means)
1. JavaCV 
PROS: FFmpegFraqmer will give us a bufferedImg per frame with little code. Actively maintained latest version came out in August. Bundles the native FFmpeg
CONS: The -platform is too big. Native crashes are harder to debug.

2. Jaffree
PROS: Clean Java API. Lighweight jar
CONS: Command-line wrapper. Releases are slow.

3. JCodec
PROS: Pure JAVA with not native dependencies. Simple FameGrab to BuffereImf API
CONS: Video support is limited to things like a H.264 main profile decoder and a baseline encoder.
