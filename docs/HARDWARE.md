# Arcus Hub OS — Hardware Reference

This document covers both hardware platforms supported by Arcus Hub OS:
the **Hub V2** (TI AM335x) and the **Hub V3** (NXP i.MX6DualLite).

---

## Hub V2 — TI AM335x (BeagleBone Black)

### SoC and Memory

| Component | Detail |
|-----------|--------|
| SoC | TI AM335x ES2.1 |
| CPU | ARM Cortex-A8, single-core |
| RAM | 512 MiB |
| Storage | eMMC on mmcblk0 |
| Machine | `beaglebone-yocto` |

There is **no SD card slot** on this hardware revision.

### Boot Chain

```
ROM → SPL/MLO (factory 2013.10, NOT updatable)
    → u-boot.img (2016.03, updatable)
    → uImage (kernel)
```

The factory-burned MLO/SPL **cannot be reflashed**. Bricking it is permanent.
The `create_update_file` script uses a pre-built U-Boot from `build-ti/tools/uboot-debug/`.

### Partition Layout (mmcblk0)

| Partition | Device | Symlink | Content |
|-----------|--------|---------|---------|
| p1 | mmcblk0p1 | — | U-Boot (FAT) |
| p2 | mmcblk0p2 | /dev/kern1 | Kernel slot A (ext2) |
| p3 | mmcblk0p3 | /dev/fs1 | Rootfs slot A (squashfs) |
| p4 | mmcblk0p4 | — | Extended partition |
| p5 | mmcblk0p5 | /dev/kern2 | Kernel slot B (ext2) |
| p6 | mmcblk0p6 | /dev/fs2 | Rootfs slot B (squashfs) |
| p7 | mmcblk0p7 | /dev/mfg | Manufacturing data (ext4) |
| p8 | mmcblk0p8 | /dev/data | Persistent data (ext4) |

A/B boot selection is controlled by `bootindex` files in each kernel partition.

### Serial Console

- UART0 at 0x44E09000 — `ttyS0` (kernel 5.4+) or `ttyO0` (kernel 4.x)
- 115200n8

### Radios

| Radio | Chip | UART | Speed | Flow Control |
|-------|------|------|-------|--------------|
| Z-Wave | ZM5304 | ttyS1 (UART1) | 115200 | None |
| Zigbee | EM3587 | ttyS2 (UART2) | 115200 | Hardware (RTS/CTS) |
| BLE | TI CC2541 | ttyS4 (UART4) | 115200 | Hardware (RTS/CTS) |

### GPIO Map

GPIOs are on the AM335x banks. Kernel 6.6 uses dynamic base allocation; the
`irisinit-ti` script looks up each bank by hardware address to compute the
correct sysfs number.

| GPIO | Bank.Pin | Function | Direction |
|------|----------|----------|-----------|
| GPIO1_12 | 1.12 | Reset button | Input (both edges) |
| GPIO1_16 | 1.16 | Zigbee reset | Output (active-low) |
| GPIO1_17 | 1.17 | Z-Wave reset | Output (active-low) |
| GPIO1_21 | 1.21 | Green LED | Output |
| GPIO1_22 | 1.22 | Yellow LED | Output |
| GPIO1_23 | 1.23 | Red LED | Output |
| GPIO1_26 | 1.26 | USB1 over-current | Input (both edges) |
| GPIO2_26 | 2.26 | BLE reset | Output (active-low) |
| GPIO2_29 | 2.29 | BLE programming data | Output |
| GPIO2_30 | 2.30 | BLE programming clock | Output |
| GPIO3_20 | 3.20 | USB0 over-current | Input (both edges) |
| GPIO3_21 | 3.21 | Battery hold | Output (keeps backup battery power on) |

GPIO bank hardware addresses used for dynamic base lookup:
- Bank 1: `4804c000`
- Bank 2: `481ac000`
- Bank 3: `481ae000`

### LEDs

Three discrete LEDs controlled via GPIO1:

| Color | GPIO | Sysfs |
|-------|------|-------|
| Green | GPIO1_21 | led2 |
| Yellow | GPIO1_22 | led3 |
| Red | GPIO1_23 | led4 |

All three solid = U-Boot is running, kernel not yet booted.

### Buzzer

PWM buzzer on `ehrpwm2B` (GPMC_AD9, pin mux mode 4). Controlled via
`/sys/class/pwm/pwmchip0/pwm1/`.

### Watchdog

Uses the AM335x internal watchdog (`/dev/watchdog`).

### UART Pin Mux

| UART | RX/TX Pins | Mode | CTS/RTS Pins | Mode |
|------|-----------|------|-------------|------|
| uart1 | UART1_RXD/TXD | 0 | — | — |
| uart2 | SPI0_SCLK/D0 | 1 | LCD_DATA8/9 | 6 |
| uart4 | GPMC_WAIT0/WPN | 6 | LCD_DATA12/13 | 6 |

### RTC

The AM335x internal RTC at 0x44E3E000 is **disabled** (not used by the hub).
Must be disabled in the device tree; on kernel 5.4, the parent `target-module@3e000`
node must also be disabled to prevent the ti-sysc driver from probing it.

---

## Hub V3 — NXP i.MX6DualLite (IMAGIC)

### SoC and Memory

| Component | Detail |
|-----------|--------|
| SoC | NXP i.MX6DualLite (i.MX6DL) / i.MX6Quad variants |
| CPU | ARM Cortex-A9, dual-core (SMP) |
| RAM | 1 GiB (base 0x10000000, size 0x40000000) |
| Storage | eMMC on mmcblk2 (8-bit, SD4/usdhc4) |
| Machine | `imxdimagic` |

Additional storage interfaces:
- **SD card slot** on usdhc3 (4-bit, removable, CD on GPIO7_0)
- **WiFi SDIO** on usdhc1 (non-removable)

### Boot Chain

```
i.MX6 Boot ROM → u-boot.imx (2016.03, on mmcblk2boot0)
               → zImage (kernel)
```

U-Boot is written to eMMC boot partition (`/dev/mmcblk2boot0`) at sector offset 2.

### Partition Layout (mmcblk2)

| Partition | Device | Symlink | Content |
|-----------|--------|---------|---------|
| p1 | mmcblk2p1 | /dev/kern1 | Kernel slot A (ext2) |
| p2 | mmcblk2p2 | /dev/fs1 | Rootfs slot A (squashfs) |
| p3 | mmcblk2p3 | /dev/kern2 | Kernel slot B (ext2) |
| p5 | mmcblk2p5 | /dev/fs2 | Rootfs slot B (squashfs) |
| p6 | mmcblk2p6 | /dev/mfg | Manufacturing data (ext4) |
| p7 | mmcblk2p7 | /dev/data | Persistent data (ext4) |

### Serial Console

- UART1 (`ttymxc0`) — 115200n8
- Note: i.MX UARTs are `ttymxc` (not `ttyS`)

### Serial Port Mapping

The `irisinit-imxdimagic` script creates symlinks so the agent uses consistent
device names across platforms:

| Function | i.MX UART | Kernel Device | Symlink |
|----------|-----------|---------------|---------|
| Console | UART1 | ttymxc0 | — |
| 4G Modem | UART2 | ttymxc1 | /dev/ttyO2 |
| Spare | UART3 | ttymxc2 | /dev/ttyO4 |
| Z-Wave | UART4 | ttymxc3 | — |
| BLE | UART5 | ttymxc4 | /dev/ttyO1 |

### Radios

| Radio | Chip | UART | Speed | Flow Control |
|-------|------|------|-------|--------------|
| Z-Wave | ZW050x | ttymxc3 (UART4) | 115200 | None |
| Zigbee | Silicon Labs (EM3587-compatible via SPI) | — | — | — |
| BLE | Broadcom (via HCI) | ttymxc4 (UART5) | 115200 | Hardware (RTS/CTS) |

### Networking

**Ethernet:**
- PHY: Qualcomm/Atheros AR8035 (RGMII)
- Controller: i.MX6 FEC
- PHY reset: GPIO4_28
- Default MAC: dc:07:c1:00:ed:85 (overridden by `/mfg/config/macAddr1`)
- **Important:** The AR8035 needs `qca,clk-out-frequency = <125000000>` in the
  device tree, or the kernel resets the 125 MHz reference clock output

**WiFi:**
- Chip: Broadcom BCM43362 via SDIO (on usdhc1/mmc0)
- Firmware: `brcmfmac43362-sdio.bin` + `.txt`
- Interface: `wlan0`
- Power GPIOs:
  - WL_REG_ON: GPIO2_0
  - WL_HOST_WAKE: GPIO2_4
  - WIFI_PWR_EN: GPIO6_16 (CUT-2 boards)

### 4G/LTE Modem

- Chip: Quectel EC25-A
- UART: ttymxc1 (UART2) with hardware flow control
- GPIOs:
  - GPIO_4G_RST: GPIO5_30
  - GPIO_4G_KEY: GPIO5_31
  - GPIO_4G_PWR_EN: GPIO6_0
  - GPIO_4G_NET_STATE: GPIO6_1

### Audio

**Codec: Nuvoton NAU8810**
- I2C address: 0x1a (on I2C1)
- MCLK: 24 MHz from CKO2 (CSI0_MCLK pad → CCM_CLKO1)
- Must use **PLL mode** (MCLK goes through codec PLL for internal DAC clocking)
- Audio interface: I2S via SSI1
- **SSI is bus master, codec is slave** (CBC_CFC mode) — codec BCLK doesn't
  reliably reach SSI in slave mode on this board
- AUDMUX routing: internal port 1 (SSI1) ↔ external port 3 (codec pads)
- Speaker output: SPKOUTP/SPKOUTN via LM4871 amplifiers (GPIO6_11, GPIO6_14)

### LED Ring

12 RGB LEDs driven by an MBI6023 LED controller via bit-banged SPI:
- CLK: GPIO5_14
- SDI: GPIO5_15
- LED_PWR_EN: GPIO4_30
- Boot state: purple (R=max, G=0, B=max) on all 12 LEDs

### Buzzer

GPIO buzzer on GPIO1_9 via custom `imagic_buzzer` kernel driver (ioctl interface).

### Watchdog

External GPIO-based watchdog:
- Feed pin: GPIO1_4
- Enable pin: GPIO1_3
- Custom kernel driver: `imagic_watchdog`

### Buttons

Polled via sysfs (not gpio-keys):

| Button | GPIO | Sysfs # |
|--------|------|---------|
| Reset | GPIO4_26 | 122 |
| Power Down | GPIO4_27 | 123 |
| Iris Button | GPIO4_31 | 127 |

### GPIO Map (Radio and Power)

**Zigbee:**
- GPIO6_2, GPIO6_3, GPIO6_4 — Zigbee control GPIOs
- GPIO6_5 — Zigbee power enable
- GPIO5_22 — Zigbee reset (CUT-2 boards)

**Z-Wave:**
- GPIO4_22 — Z-Wave reset
- GPIO4_23 — Z-Wave power enable

**BLE:**
- GPIO5_7 — BT_EN
- GPIO5_8 — BT_RESTORE
- GPIO5_9 — BT_PWR_EN
- GPIO5_17 — BT_RST

**Power:**
- GPIO4_10 — 5V0 system power enable
- GPIO1_2 — DC detect
- GPIO3_22 — USB OTG power

**Battery/Coulomb Counter:**
- GPIO4_14 — CMC_STAT
- GPIO4_15 — GG_ALRT

### NFC

- Chip: ST 95HF
- Interface: SPI via ECSPI2 (CS0, 2 MHz, CPHA=1)
- GPIOs: GPIO5_5 (IRQ_IN), GPIO5_6 (IRQ_OUT), GPIO5_12 (CS0)
- Currently disabled in software

### I2C Buses

| Bus | Speed | Devices |
|-----|-------|---------|
| I2C1 | 100 kHz | NAU8810 audio codec (0x1a) |
| I2C2 | 100 kHz | PCF8563 RTC (0x51) |
| I2C3 | 100 kHz | EDT FT5406 touchscreen (0x38, disabled) |

### Hardware Versions

The `irisinit-imxdimagic` script reads `/tmp/mfg/config/hwVer` to detect the
board revision. CUT-2 boards have different Zigbee reset wiring (GPIO5_22 vs
GPIO6_5) and additional WiFi power enable (GPIO6_16).

---

## Platform Comparison

| Feature | Hub V2 | Hub V3 |
|---------|--------|--------|
| SoC | TI AM335x (Cortex-A8) | NXP i.MX6DL (Cortex-A9 x2) |
| RAM | 512 MiB | 1 GiB |
| Storage | eMMC (mmcblk0) | eMMC (mmcblk2) |
| SD card | No | Yes |
| WiFi | No | BCM43362 (SDIO) |
| 4G modem | No | Quectel EC25-A |
| Audio | No | NAU8810 + LM4871 amplifiers |
| NFC | No | ST 95HF (disabled) |
| LEDs | 3 discrete (R/Y/G) | 12 RGB ring (MBI6023) |
| Buzzer | PWM (ehrpwm2B) | GPIO (custom driver) |
| Watchdog | Internal (AM335x) | External (GPIO-based) |
| Z-Wave | ZM5304 | ZW050x |
| Zigbee | EM3587 (UART) | Silicon Labs (SPI) |
| BLE | TI CC2541 | Broadcom (HCI UART) |
| Ethernet | Yes | AR8035 RGMII |
| Serial naming | ttyS* | ttymxc* |
| Kernel format | uImage | zImage |
| U-Boot location | FAT partition (p1) | eMMC boot partition (mmcblk2boot0) |
| Buttons | 1 (reset) | 3 (reset, power, iris) |

---

## Kernel and Software Stack

| Component | Hub V2 | Hub V3 |
|-----------|--------|--------|
| Kernel recipe | `linux-yocto` | `linux-fslc-iris` |
| Kernel version | 6.6 (Scarthgap) | 6.6 (Scarthgap) |
| U-Boot version | 2016.03 (TI patches) | 2016.03 (NXP patches) |
| Distro config | `poky-iris-ti` | `poky-iris-fsl` |
| Rootfs type | squashfs (read-only) | squashfs (read-only) |
| Image format | tar.bz2 + squashfs | tar.gz + squashfs |

Squashfs kernel support includes ZLIB and XZ compression, embedded mode with
fragment cache size 10.

### Kernel Config Fragments (Hub V2)

Located in `meta-iris/recipes-kernel/linux/linux-6.x/`:

| Fragment | Purpose |
|----------|---------|
| `serial.cfg` | 8250-OMAP serial driver, 6 UARTs |
| `debug.cfg` | Console on ttyO0+ttyS0, loglevel=7 |
| `fs.cfg` | Squashfs, MMC block (16 minors) |
| `usb.cfg` | USB networking (CDC Ethernet for LTE) |
| `adc.cfg` | TI AM335x ADC (battery voltage) |
| `gpio.cfg` | GPIO sysfs for userspace access |
| `leds.cfg` | GPIO and PWM LED drivers |
| `pwm.cfg` | TI ECAP and EHRPWM drivers |

### Hub V3 Custom Kernel Drivers

Built from `0002-IMAGIC-Hub-V3-custom-driver-support.patch`:

| Driver | Module | Purpose |
|--------|--------|---------|
| `imagic_buzzer` | `buzzer.c` | GPIO buzzer with ioctl (ON/OFF) |
| `imagic_watchdog` | `imagic_watchdog.c` | External watchdog (feed/enable via GPIO) |
| `mbi6023` | `mbi6023.c` | LED ring controller (bit-banged SPI, 39-word format) |
| `imagic_imx6_gpio` | `imagic_imx6_gpio.c` | Userspace GPIO get/set/direction |
| spidev NFC hooks | — | ST 95HF GPIO ioctls for IRQ and CS |

---

## Power and Battery Management

### Hub V2

- **Power detection:** I2C bus 0, device 0x24, register 0xA — bit 0x08 indicates AC power
- **Battery hold:** GPIO3_21 keeps backup battery regulator on (must delay 1s before
  asserting to avoid regulator glitching at boot)
- **Voltage monitoring:** 12-bit ADC via AM335x ADC
  - Voltage divider: R1=36.5k, R2=100k
  - Minimum voltage: 4.2V, no-batteries threshold: 1.25V
  - Diode drop: 0.03V (AC), 0.25V (battery)
- **USB power control:** Can disable USB ports on battery to save power; re-enables
  on AC restore
- **Status files:**
  - `/tmp/battery_on` — timestamp when battery mode entered
  - `/tmp/battery_level` — battery level 0-100
  - `/tmp/battery_voltage` — current voltage reading

### Hub V3

- **Charger IC:** SY6991 at I2C address 0x6A
  - Register 0x00 bit 0x01 = VIN_PRESENT (1=AC, 0=battery)
- **Coulomb counter:** SY6410 at I2C address 0x30
  - Registers 0x02-0x05 for voltage and charge level
  - Voltage formula: 2.50 + (raw_value * 2.50 / 0x0FFF)
  - Voltage range: min 2.68V, max 4.15V
- **USB power:** Single port controlled via GPIO 86 (`/tmp/io/usbPower`)
- **Status files:**
  - `/tmp/charge_state` — Ready/Charging/Done/Fault/Boost
  - `/tmp/charge_level` — coulomb counter percentage
- **LTE interaction:** Power loss/restore triggers LTE modem restart state machine

---

## Manufacturing Partition

The `/mfg` partition stores per-hub identity and provisioning data. It is mounted
at boot by `irisinit`, copied to `/tmp/mfg` (read-only), then unmounted.

```
/mfg/
├── config/
│   ├── model         # "IH200" (V2) or "IH304" (V3)
│   ├── customer      # "IRIS"
│   ├── batchNo       # Manufacturing batch number
│   ├── hubID         # Unique hub identifier (also used as hostname)
│   ├── hwVer         # Hardware revision (V3 CUT-1 vs CUT-2)
│   ├── macAddr1      # Primary Ethernet MAC address
│   ├── macAddr2      # Secondary MAC address
│   └── hwFlashSize   # eMMC size in bytes (computed at boot)
├── certs/            # X.509 certificates for platform TLS
└── keys/             # Private keys for hub identity
```

The hub's flash size is computed at boot from `/sys/class/block/mmcblk{0,2}/size`
and written to `/tmp/mfg/config/hwFlashSize`.

---

## Debug Interfaces

### Serial Console

The only physical debug interface. No JTAG is exposed.

| Platform | UART | Device | Baud |
|----------|------|--------|------|
| Hub V2 | UART0 | ttyS0 / ttyO0 | 115200n8 |
| Hub V3 | UART1 | ttymxc0 | 115200n8 |

For early kernel debug (before console driver loads), interrupt U-Boot and set:
```
setenv optargs "earlycon=omap8250,mmio32,0x44e09000,115200n8"   # V2 only
```

### Debug Dongle

A USB flash drive containing an RSA-signed file (`<hubID>.dbg`) that enables
SSH access on release images:

1. `irisinitd` scans `/run/media` for a file matching `<hubID>.dbg`
2. File is RSA-verified against `/etc/.ssh/iris-fwsign.pub`
3. If valid, SSH starts with password authentication and debug mode flag
   (`/tmp/debug`) is set
4. Dongle removal is detected and SSH is stopped

Agent configuration can also be loaded from the dongle (`<hubID>.cfg` → `/tmp/agent_cfg`).

### Persistent SSH

For headless access without the dongle, create `/data/config/dropbear/ssh_enabled`.
See `RELEASE.md` for details.

---

## 4G/LTE Connectivity (Hub V3)

### Supported Modems

| Modem | USB ID | Interface | Notes |
|-------|--------|-----------|-------|
| QuecTel EC25-A (mPCIe) | 2c7c:0125 | wwan0 | Primary; uses `quectel-CM` daemon |
| Huawei E3372h | 12d1:14dc | eth1 | Fixed 192.168.8.x subnet |
| ZTE WeLink ME3630 | 19d2:1476 | usb0 | AT commands on /dev/ttyUSB1 |

### Control Interface

- **Control file:** `/tmp/lteControl` — write `connect`, `disconnect`, or `init`
- **Status file:** `/tmp/lte_dongle` — modem ID when detected
- **SIM info logged:** IMEI, IMSI, ICCID, MSISDN

### Network Management

- Primary DNS: `/tmp/pri_resolv.conf`
- Backup DNS: `/tmp/bkup_resolv.conf`
- Primary gateway: `/tmp/pri_gw.conf`
- Primary availability: `/tmp/primary_available`
- Start/stop scripts: `backup_start` / `backup_stop` (SUID 4755)

---

## System Daemons

| Daemon | Purpose | Platforms |
|--------|---------|-----------|
| `irisinitd` | Main init: button/LED/SSH/provisioning handler | Both |
| `batteryd` / `batterydv3` | Battery and power management | Both |
| `dwatcher` | Daemon watchdog — restarts crashed daemons (release only) | Both |
| `iris4gd` | 4G/LTE modem management | V3 |
| `irisnfcd` | NFC daemon (currently disabled) | V3 |
| `irisagentd` | Agent launcher (starts Java agent) | Both |

`dwatcher` checks every 5 seconds (after 60s initial delay) and restarts:
`irisinitd`, `batteryd`, `iris4gd` (if present), `irisnfcd` (V3, if enabled).

---

## Radio Firmware Flash Tools

All installed to `/usr/bin/` with SUID (4755) so the agent can invoke them:

| Tool | Radio | Recipe |
|------|-------|--------|
| `zwave_flash` | Z-Wave firmware programming | `zwave-utils` |
| `zwave_nvram` | Z-Wave NVRAM access | `zwave-utils` |
| `zwave_default` | Z-Wave factory reset | `zwave-utils` |
| `zigbee_flash` | Zigbee firmware programming | `zigbee-utils` |
| `zigbee_default` | Zigbee factory reset | `zigbee-utils` |
| `zigbee_mfg_tokens` | Zigbee manufacturing tokens | `zigbee-utils` |
| `ble_prog` / `ble_ti_prog` | BLE firmware programming | `ble-utils` |

Platform-specific binaries are selected at build time (e.g., `zwave_fsl_*` for
V3 vs `zwave_ti_*` for V2).

---

## System Utilities

Installed to `/usr/bin/` or `/home/root/bin/`, many with SUID (4755):

| Utility | Purpose |
|---------|---------|
| `fwinstall` | Firmware archive installer (A/B partitions) |
| `update` | Signed firmware updater (downloads, validates, calls fwinstall) |
| `validate_image` | Validates signed firmware headers |
| `hub_restart` | Graceful reboot with LED animation |
| `factory_default` | Factory reset |
| `update_cert` / `update_key` | Certificate and key updates |
| `ledctrl` / `ringctrl` | LED control (V2 discrete / V3 ring) |
| `play_tones` | Buzzer/audio playback |
| `emmcparm` | Micron eMMC flash parameter tool |
| `wifi_scan` | WiFi network scanning (V3 only) |
| `agent_start` / `agent_stop` | Agent lifecycle management |
| `agent_install` / `agent_reinstall` | Agent installation and reset |
| `agent_reset` | Agent data reset |

---

## Key Files

### Machine and Build Configuration

| File | Purpose |
|------|---------|
| `meta-iris/conf/machine/imxdimagic.conf` | V3 machine configuration |
| `meta-iris/conf/distro/poky-iris-ti.conf` | V2 distro config (kernel version, image types) |
| `meta-iris/conf/distro/poky-iris-fsl.conf` | V3 distro config |
| `build-ti/conf/local.conf` | V2 build config (MACHINE=beaglebone-yocto) |
| `build-fsl/conf/local.conf` | V3 build config (MACHINE=imxdimagic) |

### Hardware Init and Daemons

| File | Purpose |
|------|---------|
| `meta-iris/recipes-core/iris-utils/files/irisinit-ti` | V2 hardware init (GPIO, partitions, mfg) |
| `meta-iris/recipes-core/iris-utils/files/irisinit-imxdimagic` | V3 hardware init |
| `meta-iris/recipes-core/iris-utils/files/irisinitd.c` | Main daemon (buttons, LEDs, SSH, debug dongle) |
| `meta-iris/recipes-core/iris-utils/files/batteryd.c` | V2 battery/power management |
| `meta-iris/recipes-core/iris-utils/files/batterydv3.c` | V3 battery/power management |
| `meta-iris/recipes-core/iris-utils/files/dwatcher.c` | Daemon watchdog (release images) |
| `meta-iris/recipes-core/iris-4g/files/iris4gd.c` | 4G/LTE modem daemon |
| `meta-iris/recipes-core/iris-lib/files/irisdefs.h` | Shared hardware constants and paths |

### Kernel Patches and Config

| File | Purpose |
|------|---------|
| `meta-iris/recipes-kernel/linux/linux-6.x/*.cfg` | V2 kernel config fragments |
| `meta-iris/recipes-kernel/linux/linux-fslc-lts/0001-*.patch` | V3 device tree support |
| `meta-iris/recipes-kernel/linux/linux-fslc-lts/0002-*.patch` | V3 custom kernel drivers |
| `meta-iris/recipes-kernel/linux/linux-fslc-lts/0003-*.patch` | V3 sound machine driver |
| `meta-iris/recipes-kernel/linux/linux-fslc-lts/0005-*.patch` | NAU8810 PLL BCLK fix |

### Firmware and Update Tools

| File | Purpose |
|------|---------|
| `meta-iris/recipes-core/iris-utils/files/fwinstall.c` | Firmware installer (A/B partitions) |
| `meta-iris/recipes-core/iris-utils/files/update.c` | Signed firmware updater |
| `meta-iris/recipes-core/iris-utils/files/build_image.c` | Build-time firmware signing tool |
| `meta-iris/recipes-core/iris-utils/files/validate_image.c` | Runtime image validation |
| `meta-iris/recipes-core/iris-lib/files/irisversion.h` | Firmware version definition |
