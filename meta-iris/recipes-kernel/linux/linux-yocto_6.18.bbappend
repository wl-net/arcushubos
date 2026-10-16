FILESEXTRAPATHS:prepend := "${THISDIR}/linux-6.x:"

# Clear out the kernel extra features to make sure netfilter support doesn't
#  get added back in!
KERNEL_EXTRA_FEATURES = ""
KERNEL_FEATURES:append = ""

# Machine config. KBRANCH/SRCREV intentionally not pinned here: oe-core's
# linux-yocto_6.18.bb supplies a self-consistent KBRANCH + SRCREV + LINUX_VERSION,
# and meta-yocto's own 6.18 bbappend supplies KMACHINE for beaglebone.
KMACHINE:beaglebone-yocto ?= "beaglebone"
COMPATIBLE_MACHINE:beaglebone-yocto = "beaglebone-yocto"

# Create a uImage output file — old U-Boot on Hub V2 requires this
KERNEL_IMAGETYPE = "uImage"
KERNEL_EXTRA_ARGS += "LOADADDR=0x80008000"

SRC_URI += " \
	file://defconfig \
	file://adc.cfg \
	file://fs.cfg \
	file://leds.cfg \
	file://pwm.cfg \
	file://usb.cfg \
	file://serial.cfg \
	file://debug.cfg \
	file://gpio.cfg \
	file://nosound.cfg \
	file://0001-Iris-dtsi-config-changes.patch \
	file://0002-Disable-Ethernet-MDIX.patch \
	"
