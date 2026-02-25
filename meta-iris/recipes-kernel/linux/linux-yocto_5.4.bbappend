FILESEXTRAPATHS_prepend := "${THISDIR}/linux-5.x:"

# Clear out the kernel extra features to make sure netfilter support doesn't
#  get added back in!
KERNEL_EXTRA_FEATURES = ""
KERNEL_FEATURES_append = ""

# Kernel branch/machine config (replaces masked meta-yocto-bsp bbappend)
KBRANCH_beaglebone-yocto = "v5.4/standard/beaglebone"
KMACHINE_beaglebone-yocto ?= "beaglebone"
SRCREV_machine_beaglebone-yocto = "fe901e2f4b156e9cf7ddb03f479f7339d28e398b"
LINUX_VERSION_beaglebone-yocto = "5.4.273"
LINUX_VERSION = "5.4.273"
PV = "${LINUX_VERSION}+git${SRCPV}"
COMPATIBLE_MACHINE_beaglebone-yocto = "beaglebone-yocto"

# Create a uImage output file to match what we have done in past
KERNEL_IMAGETYPE = "uImage"

SRC_URI += " \
	file://defconfig \
	file://adc.cfg \
	file://fs.cfg \
	file://leds.cfg \
	file://pwm.cfg \
	file://usb.cfg \
	file://serial.cfg \
	file://debug.cfg \
	file://0001-Iris-dtsi-config-changes.patch \
	file://0004-Go-back-to-old-mmc-numbering-scheme.patch \
	file://0002-Disable-Ethernet-MDIX.patch \
	"

# This issue appears to have been fixed in another manner
#	file://0003-usbnet-fix-debugging-output-for-work-items.patch

