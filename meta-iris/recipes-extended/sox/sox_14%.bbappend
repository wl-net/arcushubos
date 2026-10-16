
# Strip down package, we just need MP3 and AMR related support
# autoconf-archive-native must stay: it supplies the AX_APPEND_COMPILE_FLAGS macros,
# and without it autoreconf leaves them unexpanded in configure (shell syntax error).
# libpng/libsndfile1 are intentionally dropped; those features are not needed.
DEPENDS = "autoconf-archive-native libtool"
PACKAGECONFIG = "alsa mad amrnb amrwb"

# flac/oggvorbis are disabled by omitting them from PACKAGECONFIG above, and
# sndfile auto-disables now that libsndfile1 is off DEPENDS. sox's configure has
# no --without- option for any of the three, and passing them fails QA.
EXTRA_OECONF = "--without-png"