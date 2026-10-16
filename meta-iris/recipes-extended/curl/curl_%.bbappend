
# Use openssl instead of gnutls to avoid pulling in extra dependencies
PACKAGECONFIG:class-target = "${@bb.utils.filter('DISTRO_FEATURES', 'ipv6', d)} openssl proxy threaded-resolver zlib"
