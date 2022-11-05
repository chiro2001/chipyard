#define XSTATUS_FS         (3 << 13) /* initial state of FPU, clear to disable */
#define XSTATUS_PUM        (1 << 18)
#include "cpuport.h"
#include "encoding.h"

#define __STACKSIZE__ 0x400

// #include "firmware.h"
.section .text
.global _start
_start:
	.global main

	// for sifive uart
  lui a0,0x54000000>>12
	// disable interrupts
	li a1,0
	sw a1,16(a0)
	// enable tx
	li a1,1
	sw a1,8(a0)
	// enable rx
	li a1,1
	sw a1,12(a0)

	// print START\n
	addi a1,zero,'S'
	addi a2,zero,'T'
	addi a3,zero,'A'
	addi a4,zero,'R'
	addi a5,zero,'T'
	addi a6,zero,'\n'
  sw a1,0(a0)
	sw a2,0(a0)
	sw a3,0(a0)
	sw a4,0(a0)
	sw a5,0(a0)
	sw a6,0(a0)
  # j _start
  li  x1, 0
  li  x2, 0
  li  x3, 0
  li  x4, 0
  li  x5, 0
  li  x6, 0
  li  x7, 0
  li  x8, 0
  li  x9, 0
  li  x10,0
  li  x11,0
  li  x12,0
  li  x13,0
  li  x14,0
  li  x15,0
  li  x16,0
  li  x17,0
  li  x18,0
  li  x19,0
  li  x20,0
  li  x21,0
  li  x22,0
  li  x23,0
  li  x24,0
  li  x25,0
  li  x26,0
  li  x27,0
  li  x28,0
  li  x29,0
  li  x30,0
  li  x31,0

  # setup stacks per hart
  csrr t0, mhartid                  # read current hart id
  slli t0, t0, 10                   # shift left the hart id by 1024

  # park harts with id != 0
  csrr a0, mhartid                  # read current hart id
  bnez a0, park                     # if we're not on the hart 0

  csrw SRC_XIE, 0                   # clear Interrupt Registers
  csrw SRC_XIP, 0

  la t0, trap_entry
  csrw SRC_XTVEC, t0                # set Trap Vector Base Address Register

  /* set to disable FPU */
  li t0, XSTATUS_FS                 # close fpu
  csrc SRC_XSTATUS, t0

.option push
.option norelax
  la gp, __global_pointer$
.option pop

  la   sp, __stack_start__
  li   t0, __STACKSIZE__
  add  sp, sp, t0
  csrw SRC_XSCRATCH, sp
  jal main
	j park

reset_vec:
	j _start

trap_entry:
	.global park
	j park

# park:
# 	wfi
# 	j park
