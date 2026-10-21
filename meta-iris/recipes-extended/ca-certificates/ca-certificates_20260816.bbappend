# IRIS - pull in Debian 20260816+nmu1 ahead of oe-core, which blacklists
# "emSign ECC Root CA - G3" and "Comodo AAA Services root" (Debian #1151413).
# Remove this file once oe-core wrynose upgrades past 20260816; the dangling
# bbappend parse error is the reminder.
PV = "20260816+nmu1"
SRC_URI[sha256sum] = "914416a1e44a25733aff51092fe4e27fdec1c9f6e8d1db0b8364922486153134"
