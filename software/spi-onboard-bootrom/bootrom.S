#include "address.h"

.section .start, "ax", @progbits
.global _start
_start:
	// for sifive uart
  li s5,0x54000000
#if(ENABLE_CORES)
	li t1,FLASH_DONE
	sw zero,0(t1)
	sync
#endif
	csrr a0, mhartid                  # read current hart id
  bnez a0, park                     # if we're not on the hart 0
	// disable interrupts
	li a1,0
	sw a1,16(s5)
	// enable tx
	li a1,1
	sw a1,8(s5)
	// enable rx
	li a1,1
	sw a1,12(s5)

	// print B
	addi s3,zero,'B'
	sw s3,0(s5)
	j start_copy

_copy_flash:
	// flash_addr
	li a1, 0x20100000
	// ddr_addr
	li a2, 0x80000000
	// program_size
	# li a3, (15*0x400*0x400)
	li a3, ((64 * 0x400) - 4) // set to 64kb
_copy_flash_loop:
	lw t0, 0(a1)
	sw t0, 0(a2)
	addi a1, a1, 4
	addi a2, a2, 4
	addi a3, a3, -4
	bnez a3, _copy_flash_loop
#if(ENABLE_CORES)
	li t1,FLASH_DONE
	sw t1,0(t1)
	sync
#endif
	ret

start_copy:
	jal _copy_flash

copy_done:
	// print D
	addi a1,zero,'D'
	sw a1,0(s5)

  li a0, BOOTADDR_REG
  lw a0, 0(a0)
  csrw mepc, a0       // return from interrupt to start of user program
  csrr a0, mhartid    // hartid for next level bootloader
  la a1, _dtb         // dtb address for next level bootloader
  li a2, 0x80         // set mstatus MPIE to 0
  csrc mstatus, a2
  mret

.align 3
_dtb:

park:
#if(ENABLE_CORES)
	li t1,FLASH_DONE
	sync
	lw t0,0(t1)
	beqz t0,park
	j copy_done
#else
	wfi
	j park
#endif