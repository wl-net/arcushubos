FILESEXTRAPATHS:prepend := "${THISDIR}/linux-6.x:"

# Clear out the kernel extra features to make sure netfilter support doesn't
#  get added back in!
KERNEL_EXTRA_FEATURES = ""
KERNEL_FEATURES:append = ""

# Kernel branch/machine config (replaces masked meta-yocto-bsp bbappend)
KBRANCH:beaglebone-yocto = "v6.6/standard/beaglebone"
KMACHINE:beaglebone-yocto ?= "beaglebone"
SRCREV_machine:beaglebone-yocto = "06644f0d7193d7ec39d7fe41939a21953e7a0c65"
LINUX_VERSION:beaglebone-yocto = "6.6.21"
LINUX_VERSION = "6.6.21"
PV = "${LINUX_VERSION}+git${SRCPV}"
COMPATIBLE_MACHINE:beaglebone-yocto = "beaglebone-yocto"

# Create a uImage output file — old U-Boot on Hub V2 requires this
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
	file://0003-Fix-mmc-numbering-via-DT-aliases.patch \
	file://0002-Disable-Ethernet-MDIX.patch \
	"
