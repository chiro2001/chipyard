# bootrom_img = start.rv32.img start.rv64.img
# bootrom_dump = start.rv32.dump start.rv64.dump
bootrom_img = start.rv32.img
bootrom_dump = start.rv32.dump

CROSS_COMPILE := riscv32-unknown-elf-
GCC=$(CROSS_COMPILE)gcc
CFLAGS_RV32=-mabi=ilp32 -march=rv32im
CFLAGS_RV64=-mabi=lp64 -march=rv64im
OBJCOPY=$(CROSS_COMPILE)objcopy
OBJDUMP=$(CROSS_COMPILE)objdump

START_FILES := print.c main.c hello.c sifive-uart.c

default: img dump

img: $(bootrom_img)

dump: $(bootrom_dump)

%.img: %.elf
	$(OBJCOPY) -O binary --change-addresses=-0x10000 $< $@

start.rv32.elf: start.S linker.ld
	$(GCC) $(CFLAGS_RV32) -Tlinker.ld start.S $(START_FILES) -nostdlib -static -o start.rv32.elf

%.rv32.elf: %.S linker.ld
	$(GCC) $(CFLAGS_RV32) -Tlinker.ld $< -nostdlib -static -o $@

start.rv64.elf: start.S linker.ld
	$(GCC) $(CFLAGS_RV64) -Tlinker.ld start.S $(START_FILES) -nostdlib -static -o start.rv64.elf

%.rv64.elf: %.S linker.ld
	$(GCC) $(CFLAGS_RV64) -Tlinker.ld $< -nostdlib -static -o $@

%.dump: %.elf
	$(OBJDUMP) -d $< > $@

clean:
	rm -f *.elf *.dump *.img
