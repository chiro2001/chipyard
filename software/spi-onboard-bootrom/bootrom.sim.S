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

	// print START\n
	addi a1,zero,'S'
	addi a2,zero,'T'
	addi a3,zero,'A'
	addi a4,zero,'R'
	addi a5,zero,'T'
	addi a6,zero,'\n'
  sw a1,0(s5)
	sw a2,0(s5)
	sw a3,0(s5)
	sw a4,0(s5)
	sw a5,0(s5)
	sw a6,0(s5)
	j start_copy

// this boot ROM doesn't know about any boot devices, so it just spins,
// waiting for the serial interface to load the program and interrupt it
.section .text.hang, "ax", @progbits
.globl _hang
_hang: // reset vector
  la a0, _start          // on MSIP interrupt, go to _start
  csrw mtvec, a0
  csrr t0, misa          // get MISA mask
  srai t0, t0, 'S' - 'A' // get 'S' supervisor bit
  andi t0, t0, 0x1
  beqz t0, 1f            // only zero mideleg if supported
  csrw mideleg, zero     // no delegation
1:
  li a0, 8               // MIE or MSIP bit
  csrw mie, a0           // set only MSIP in MIE CSR
  csrs mstatus, a0       // set MIE in mstatus CSR

	li s5,0x54000000
#if(ENABLE_CORES)
	li t1,FLASH_DONE
	sw zero,0(t1)
	sync
#endif
	csrr a0, mhartid                  # read current hart id
  bnez a0, park                     # if we're not on the hart 0

wfi_loop: // wait for MSIP interrupt to start program execution
  wfi
  j wfi_loop

_copy_flash:
	// flash_addr
	li a1, 0x20100000
	// ddr_addr
	li a2, 0x80000000
	// program_size
	# li a3, (15*0x400*0x400)
	li a3, ((40 * 0x400) - 4) // set to 40kb
	# li a3, 4 // use ddr loader
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
	csrr a0, mhartid
	bnez a0, attach
  addi a1,zero,'C'
	addi a2,zero,'O'
	addi a3,zero,'P'
	addi a4,zero,'Y'
	addi a5,zero,'.'
	addi a6,zero,'\n'
  sw a1,0(s5)
	sw a2,0(s5)
	sw a3,0(s5)
	sw a4,0(s5)
	sw a5,0(s5)
	sw a6,0(s5)
	j attach_done
attach:
	addi a1,zero,'A'
	addi a2,zero,'T'
	addi a3,zero,'A'
	addi a4,zero,'C'
	addi a5,zero,'H'
	addi a6,zero,'\n'
  sw a1,0(s5)
	sw a2,0(s5)
	sw a3,0(s5)
	sw a4,0(s5)
	sw a5,0(s5)
	sw a6,0(s5)
attach_done:
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
	li t2, 0x3f
pack_loop:
	addi t2, t2, -1
	bnez t2, pack_loop
	li t1,FLASH_DONE
	sync
	lw t0,0(t1)
	beqz t0,park
	j copy_done
#else
	wfi
	j park
#endif