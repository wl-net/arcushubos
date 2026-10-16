#
# Install wireless firmware files we need on the hub - a very small
#  subset of the available "linux-firmware" package!
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

SUMMARY = "Wireless firmware files for use with hub Linux kernel"
SECTION = "kernel"

# The Realtek firmware and both licence texts come from oe-core's linux-firmware,
# which is packaged per-device and built from a release tarball. This recipe used
# to clone the full linux-firmware git history (~10GB) to cherry-pick those same
# files, and its brcm copies were overwritten by the local ones below anyway.
RDEPENDS:${PN} = " \
    linux-firmware-rtl8188 \
    linux-firmware-rtl8192cu \
    linux-firmware-rtl-license \
    linux-firmware-broadcom-license \
"

# Broadcom AP6181 (43362) support. Upstream linux-firmware has no
# brcm/brcmfmac43362-sdio.bin any more (it moved to cypress/cyfmac43362-sdio.*),
# and ships only board-specific NVRAM .txt files, none of which match this
# hardware -- so both of these must be carried locally.
#   .bin: later firmware supplied by Broadcom via GS
#   .txt: NVRAM needed to bring the driver up
SRC_URI = "file://brcmfmac43362-sdio.txt \
           file://brcmfmac43362-sdio.bin \
           "

S = "${UNPACKDIR}"

# Vendor-supplied binary with no redistributable licence text of its own; the
# Broadcom licence ships on target via linux-firmware-broadcom-license above.
LICENSE = "CLOSED"

PE = "1"
PV = "1.0"

FILES:${PN} = "/lib/firmware/brcm/brcmfmac43362-sdio.bin \
               /lib/firmware/brcm/brcmfmac43362-sdio.txt \
               "

do_install() {
	install -d ${D}${base_libdir}/firmware/brcm/
	install -m 0644 ${UNPACKDIR}/brcmfmac43362-sdio.bin ${D}${base_libdir}/firmware/brcm/
	install -m 0644 ${UNPACKDIR}/brcmfmac43362-sdio.txt ${D}${base_libdir}/firmware/brcm/
}
