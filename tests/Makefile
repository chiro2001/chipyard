# USE_64BIT := 1

ifdef USE_64BIT
CROSS_COMPILE := riscv64-unknown-elf
CFLAGS := 
else
CROSS_COMPILE := riscv32-unknown-elf
CFLAGS := -march=rv32ima -mabi=ilp32
endif
GCC := $(CROSS_COMPILE)-gcc
OBJDUMP := $(CROSS_COMPILE)-objdump
CFLAGS += -std=gnu99 -O2 -fno-common -fno-builtin-printf -Wall
LDFLAGS := -static

include libgloss.mk

PROGRAMS = pwm blkdev accum charcount nic-loopback big-blkdev pingd \
           streaming-passthrough streaming-fir nvdla spiflashread spiflashwrite fft gcd

spiflash.img: spiflash.py
	python3 $<

.DEFAULT_GOAL := default

.PHONY: default
default: $(addsuffix .riscv,$(PROGRAMS)) spiflash.img

.PHONY: dumps
dumps: $(addsuffix .dump,$(PROGRAMS))

%.o: %.S
	$(GCC) $(CFLAGS) -D__ASSEMBLY__=1 -c $< -o $@

%.o: %.c mmio.h spiflash.h
	$(GCC) $(CFLAGS) -c $< -o $@

%.riscv: %.o $(libgloss)
	$(GCC) $(LDFLAGS) $< -o $@

%.dump: %.riscv
	$(OBJDUMP) -D $< > $@


.PHONY: clean
clean:
	rm -f *.riscv *.o *.dump
	$(if $(libgloss),rm -rf $(libgloss_builddir)/)
