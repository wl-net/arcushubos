#
# Create various IRIS init/utility programs
#

#
# Copyright 2019 Arcus Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

DESCRIPTON = "Iris init and utility programs"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/Apache-2.0;md5=89aea4e17d99a7cacdbeed46a0096b10"

DEPENDS = "glib-2.0 iris-lib"
DEPENDS:append:class-target = " iris-utils-native"
RDEPENDS:iris-utils = "iris-lib cronie curl"
PR = "r0"
S = "${UNPACKDIR}"
BBCLASSEXTEND += "native"

INITSCRIPT_NAME = "irisinit"
INITSCRIPT_PARAMS = "start 95 S ."

# the above init param sets up
# /etc/rcS.d# ls -ltr S95irisinit
# lrwxrwxrwx 1 root root 18 Feb 31  2000 S95irisinit -> ../init.d/irisinit

inherit autotools update-rc.d useradd pkgconfig

# These have no configure script (sources are plain .c files built by the
# hand-written do_compile below), so autotools' oe_runconf would fatal.
do_configure[noexec] = "1"

SRC_URI = "file://ledctrl.c \
           file://fwinstall.c \
           file://boot_partition \
           file://build_image.h \
           file://build_image.c \
           file://validate_image.c \
           file://update.c \
           file://iris-fwsign-nopass.key \
           file://iris-fwsign.pub \
           file://iris-hubsign.chained.crt \
           file://irisinit-ti \
           file://irisinit-imxdimagic \
           file://irisinitd.c \
           file://daemon.c \
           file://daemon.h \
           file://auto_updated.c \
           file://batteryd.c \
           file://hub_restart.c \
           file://update_cert.c \
           file://update_key.c \
           file://factory_default.c \
           file://play_tones.c \
           file://securetty \
           file://securetty_noconsole \
           file://hosts \
           file://ntpd \
           file://fstab \
           file://ifplugd \
           file://ifplugd.action \
           file://ifplugd-wifi.action \
           file://ifplugd.conf \
           file://emmcparm_1.0.c \
           file://0daily \
           file://0hourly \
           file://fstrim.cron \
           file://ntpd.cron \
           file://reboot_sound.txt \
           file://reset_sound.txt \
           file://profile \
           file://interfaces \
           file://wifi_start \
           file://wifi_test \
           file://wpa_supplicant.conf \
           file://asound-imx6qmq.conf \
           file://ledRing.c \
           file://ringctrl.c \
           file://batterydv3.c \
           file://interfaces_mfg \
           file://resolv_mfg \
           file://wifi_scan.c \
           file://dwatcher.c \
           file://zigbee-firmware-hwflow.bin \
           file://agent_install \
           file://agent_reinstall \
           file://agent_reset \
           file://agent_start \
           file://agent_stop \
           file://firmware_install \
           "

# You must set USERADD_PACKAGES when you inherit useradd. This
# lists which output packages will include the user/group
# creation code.
USERADD_PACKAGES = "${PN}"

# USERADD_PARAM specifies command line options to pass to the
# useradd command. Multiple users can be created by separating
# the commands with a semicolon.
USERADD_PARAM:${PN} = "-u 1000 -d /home/agent -s /bin/sh -G dialout,audio agent"

# Consider any warnings errors (well, not ignored results or format truncation)
CFLAGS += "-Wall -Werror -Wno-unused-result -Wno-format-truncation -Wno-stringop-truncation"

# Add platform specific defines
TARGET_MACHINE := "${@'${MACHINE}'.replace('-', '_')}"
CFLAGS += "-D${TARGET_MACHINE}"

# We have to grab the include files from the iris-lib package for host build
#  as these do not get installed for host access
HOSTCFLAGS = "-Wall -Werror -Wno-unused-result -D${TARGET_MACHINE} \
            -I${THISDIR}/../iris-lib/files \
            "

do_compile () {
        ${CC} ${CFLAGS} ${LDFLAGS} ${UNPACKDIR}/irisinitd.c ${UNPACKDIR}/daemon.c -o irisinitd -lrt -lpthread -liris `/usr/bin/pkg-config --cflags --libs glib-2.0`

# Updates are now going to be handled by hub agent
#       ${CC} ${CFLAGS} ${LDFLAGS} ${UNPACKDIR}/auto_updated.c ${UNPACKDIR}/daemon.c -o auto_updated -liris
        ${CC} ${CFLAGS} ${LDFLAGS} ${UNPACKDIR}/validate_image.c -o validate_image
        ${CC} ${CFLAGS} ${LDFLAGS} ${UNPACKDIR}/update.c -o update -liris
        # Battery daemon is machine specific
        if [ "${MACHINE}" = "beaglebone-yocto" ]; then
           ${CC} ${CFLAGS} ${LDFLAGS} ${UNPACKDIR}/batteryd.c -o batteryd -liris
        elif [ "${MACHINE}" = "imxdimagic" ]; then
           ${CC} ${CFLAGS} ${LDFLAGS} ${UNPACKDIR}/batterydv3.c -o batteryd -liris
        fi
        ${CC} ${CFLAGS} ${LDFLAGS} ${UNPACKDIR}/fwinstall.c -o fwinstall -liris
        ${CC} ${CFLAGS} ${LDFLAGS} ${UNPACKDIR}/hub_restart.c -o hub_restart
        ${CC} ${CFLAGS} ${LDFLAGS} ${UNPACKDIR}/update_cert.c -o update_cert -liris
        ${CC} ${CFLAGS} ${LDFLAGS} ${UNPACKDIR}/update_key.c -o update_key -liris
        ${CC} ${CFLAGS} ${LDFLAGS} ${UNPACKDIR}/factory_default.c -o factory_default -liris
        ${CC} ${CFLAGS} ${LDFLAGS} ${UNPACKDIR}/play_tones.c -o play_tones
        ${CC} ${CFLAGS} ${LDFLAGS} ${UNPACKDIR}/dwatcher.c -o dwatcher -liris

        # LED Ring support vs. plain LEDs
        if [ "${MACHINE}" = "imxdimagic" ]; then
           ${CC} ${CFLAGS} ${LDFLAGS} ${UNPACKDIR}/ledRing.c -o ledRing -liris
           ${CC} ${CFLAGS} ${LDFLAGS} ${UNPACKDIR}/ringctrl.c -o ringctrl
        else
           ${CC} ${CFLAGS} ${LDFLAGS} ${UNPACKDIR}/ledctrl.c -o ledctrl
        fi

        # Wifi scan support
        if [ "${MACHINE}" != "beaglebone-yocto" ]; then
           ${CC} ${CFLAGS} ${LDFLAGS} ${UNPACKDIR}/wifi_scan.c -o wifi_scan
        fi

        # Micron eMMC flash tool - don't treat warnings as errors!
        ${CC} ${LDFLAGS} ${UNPACKDIR}/emmcparm_1.0.c -o emmcparm
}

do_compile:class-native() {
        # This is a host tool
        gcc ${HOSTCFLAGS} ${UNPACKDIR}/build_image.c -o build_image
        gcc ${HOSTCFLAGS} ${UNPACKDIR}/validate_image.c -o validate_image
}

FILES:${PN} += "/data \
	       /mfg \
	       /home/root/bin/ \
	       /home/root/etc/ \
	       /home/root/sounds/ \
	       /home/root/fw/ \
	       "

do_install () {
	install -d ${D}${bindir}
	install -m 0755 irisinitd ${D}${bindir}
# Updates are now going to be handled by the hub agent
#	install -m 0755 auto_updated ${D}${bindir}
	install -m 0755 batteryd ${D}${bindir}
	install -m 0755 dwatcher ${D}${bindir}

	# This need to have suid set so agent can use them
	install -m 4755 validate_image ${D}${bindir}
	install -m 4755 update ${D}${bindir}
	install -m 4755 fwinstall ${D}${bindir}
	install -m 4755 hub_restart ${D}${bindir}
	install -m 4755 update_cert ${D}${bindir}
	install -m 4755 update_key ${D}${bindir}
	install -m 4755 factory_default ${D}${bindir}
	install -m 4755 play_tones ${D}${bindir}
	install -m 4755 emmcparm ${D}${bindir}

	# LED Ring support vs. plain LEDs
	if [ "${MACHINE}" = "imxdimagic" ]; then
	   install -m 4755 ledRing ${D}${bindir}
	   install -m 4755 ringctrl ${D}${bindir}
	else
	   install -m 4755 ledctrl ${D}${bindir}
	fi

	install -m 0755 ${UNPACKDIR}/boot_partition ${D}${bindir}
	install -d ${D}${sysconfdir}/init.d

	# Init script depends on machine
	if [ "${MACHINE}" = "beaglebone-yocto" ]; then
           install -m 0755 ${UNPACKDIR}/irisinit-ti  ${D}${sysconfdir}/init.d/irisinit
	elif [ "${MACHINE}" = "imxdimagic" ]; then
           install -m 0755 ${UNPACKDIR}/irisinit-imxdimagic  ${D}${sysconfdir}/init.d/irisinit
	fi

	# Create data and mfg partition mount points
	install -d ${D}/data
	install -d ${D}/mfg

	# Install certs
	install -d ${D}/etc/.ssh
	install -m 0644 ${UNPACKDIR}/iris-fwsign.pub ${D}/etc/.ssh
	install -m 0644 ${UNPACKDIR}/iris-hubsign.chained.crt ${D}/etc/.ssh

	# Copy hosts, fstab, profile, asound.conf and securetty files for later use
	install -d ${D}/home/root/etc/
	install -m 0644 ${UNPACKDIR}/hosts ${D}/home/root/etc/
	install -m 0444 ${UNPACKDIR}/securetty ${D}/home/root/etc/
	install -m 0444 ${UNPACKDIR}/securetty_noconsole ${D}/home/root/etc/
	install -m 0644 ${UNPACKDIR}/fstab ${D}/home/root/etc/
	install -m 0644 ${UNPACKDIR}/profile ${D}/home/root/etc/
	if [ "${MACHINE}" = "imxdimagic" ]; then
           install -m 0755 ${UNPACKDIR}/asound-imx6qmq.conf ${D}/home/root/etc/asound.conf
	fi

	# Set up wireless configuration files
	if [ "${MACHINE}" != "beaglebone-yocto" ]; then
	   install -m 0644 ${UNPACKDIR}/interfaces ${D}/home/root/etc/
	   install -m 0755 ${UNPACKDIR}/wpa_supplicant.conf ${D}/home/root/etc/
	   install -m 0755 ${UNPACKDIR}/wifi_start ${D}${bindir}
	   install -m 0755 ${UNPACKDIR}/wifi_test ${D}${bindir}
	   install -m 0755 wifi_scan ${D}${bindir}
	fi

	# Set files needed for manufacturing of v3 hub
	if [ "${MACHINE}" = "imxdimagic" ]; then
	   install -m 0644 ${UNPACKDIR}/interfaces_mfg ${D}/home/root/etc/
	   install -m 0644 ${UNPACKDIR}/resolv_mfg ${D}/home/root/etc/
	fi

	# Copy sound files
	install -d ${D}/home/root/sounds/
	install -m 0444 ${UNPACKDIR}/reboot_sound.txt ${D}/home/root/sounds/
	install -m 0444 ${UNPACKDIR}/reset_sound.txt ${D}/home/root/sounds/

	# Radio firmware to install after boot (only for v3 hub)
	if [ "${MACHINE}" = "imxdimagic" ]; then
	   install -d ${D}/home/root/fw/
	   install -m 0444 ${UNPACKDIR}/zigbee-firmware-hwflow.bin ${D}/home/root/fw/
	fi

	# Replace /etc/timestamp with a link to /data/config/timestamp
	ln -s /data/config/timestamp ${D}${sysconfdir}/timestamp

	# Install ntpd startup script
	install -d ${D}${sysconfdir}/network/if-up.d
	install -m 0755 ${UNPACKDIR}/ntpd  ${D}${sysconfdir}/network/if-up.d/

	# Install ifplugd support
	install -d ${D}${sysconfdir}/ifplugd
	install -m 0755 ${UNPACKDIR}/ifplugd  ${D}${sysconfdir}/init.d/
	# Allow for wifi support on non-TI platforms
	if [ "${MACHINE}" != "beaglebone-yocto" ]; then
	  install -m 0755 ${UNPACKDIR}/ifplugd-wifi.action ${D}${sysconfdir}/ifplugd/ifplugd.action
	else
	  install -m 0755 ${UNPACKDIR}/ifplugd.action ${D}${sysconfdir}/ifplugd/
	fi
	install -m 0755 ${UNPACKDIR}/ifplugd.conf ${D}${sysconfdir}/ifplugd/

	# Install cron setup
	install -d ${D}${sysconfdir}/cron.d
	install -d ${D}${sysconfdir}/cron.daily
	install -d ${D}${sysconfdir}/cron.hourly

	# We run fstrim every hour now
	install -m 0444 ${UNPACKDIR}/0hourly  ${D}${sysconfdir}/cron.d/
	install -p -m 4755 ${UNPACKDIR}/fstrim.cron ${D}${sysconfdir}/cron.hourly/fstrim

	# Run ntpd every hour as well
	install -p -m 4755 ${UNPACKDIR}/ntpd.cron ${D}${sysconfdir}/cron.hourly/ntpd

	# Create a link to arp - so it's in "normal" location
	install -d ${D}/usr/sbin
	ln -s /sbin/arp ${D}/usr/sbin/arp

	# Copy developer scripts
	install -d ${D}/home/root/bin/
	install -m 0755 ${UNPACKDIR}/agent_install ${D}/home/root/bin/
	install -m 0755 ${UNPACKDIR}/agent_reinstall ${D}/home/root/bin/
	install -m 0755 ${UNPACKDIR}/agent_reset ${D}/home/root/bin/
	install -m 0755 ${UNPACKDIR}/agent_start ${D}/home/root/bin/
	install -m 0755 ${UNPACKDIR}/agent_stop ${D}/home/root/bin/
	install -m 0755 ${UNPACKDIR}/firmware_install ${D}/home/root/bin/
}


do_install:class-native() {
	install -d ${D}${bindir}

	# We install the host tools
	install -m 0755 build_image ${D}${bindir}
	install -m 0755 validate_image ${D}${bindir}
}
