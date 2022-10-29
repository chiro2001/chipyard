// #include "firmware.h"
.section .text
	.global main

_start:
/* print "DONE\n" */
	lui a0,0x10000000>>12
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
	/* set stack pointer */
	lui sp,(128*1024)>>12

	/* call C code */
	jal ra,main

loop:
	wfi
	j loop