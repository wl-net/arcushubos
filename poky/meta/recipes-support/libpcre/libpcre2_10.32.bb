DESCRIPTION = "There are two major versions of the PCRE library. The \
newest version is PCRE2, which is a re-working of the original PCRE \
library to provide an entirely new API. The original, very widely \
deployed PCRE library's API and feature are stable, future releases \
 will be for bugfixes only. All new future features will be to PCRE2, \
not the original PCRE 8.x series."
SUMMARY = "Perl Compatible Regular Expressions version 2"
HOMEPAGE = "http://www.pcre.org"
SECTION = "devel"
LICENSE = "BSD"
LIC_FILES_CHKSUM = "file://LICENCE;md5=cf66d307bf03bae65d413eb7a8e603a0"

SRC_URI = "https://ftp.pcre.org/pub/pcre/pcre2-${PV}.tar.bz2 \
           file://pcre-cross.patch \
"

SRC_URI[md5sum] = "8a096287153fb994970df3570e90fcb5"
SRC_URI[sha256sum] = "f29e89cc5de813f45786580101aaee3984a65818631d4ddbda7b32f699b87c2e"

CVE_PRODUCT = "pcre2"

S = "${WORKDIR}/pcre2-${PV}"

PROVIDES += "pcre2"
DEPENDS += "bzip2 zlib"

BINCONFIG = "${bindir}/pcre2-config"

inherit autotools binconfig-disabled

EXTRA_OECONF = "\
    --enable-newline-is-lf \
    --enable-rebuild-chartables \
    --with-link-size=2 \
    --with-match-limit=10000000 \
    --enable-pcre2-16 \
    --enable-pcre2-32 \
"
# Set LINK_SIZE in BUILD_CFLAGS given that the autotools bbclass use it to
# set CFLAGS_FOR_BUILD, required for the libpcre build.
BUILD_CFLAGS =+ "-DLINK_SIZE=2 -I${B}/src"
CFLAGS += "-D_REENTRANT"
CXXFLAGS_append_powerpc = " -lstdc++"

export CCLD_FOR_BUILD ="${BUILD_CCLD}"

PACKAGES =+ "libpcre2-16 libpcre2-32 pcre2grep pcre2grep-doc pcre2test pcre2test-doc"

SUMMARY_pcre2grep = "grep utility that uses perl 5 compatible regexes"
SUMMARY_pcre2grep-doc = "grep utility that uses perl 5 compatible regexes - docs"
SUMMARY_pcre2test = "program for testing Perl-comatible regular expressions"
SUMMARY_pcre2test-doc = "program for testing Perl-comatible regular expressions - docs"

FILES_libpcre2-16 = "${libdir}/libpcre2-16.so.*"
FILES_libpcre2-32 = "${libdir}/libpcre2-32.so.*"
FILES_pcre2grep = "${bindir}/pcre2grep"
FILES_pcre2grep-doc = "${mandir}/man1/pcre2grep.1"
FILES_pcre2test = "${bindir}/pcre2test"
FILES_pcre2test-doc = "${mandir}/man1/pcre2test.1"

BBCLASSEXTEND = "native nativesdk"
