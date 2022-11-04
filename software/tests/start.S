// #include "firmware.h"
.section .text
	.global main

_start:
/* print "START\n" */
	lui a0,0x54000000>>12
	addi a1,zero,'S'
	addi a2,zero,'T'
	addi a3,zero,'A'
	addi a4,zero,'R'
	addi a5,zero,'T'
	addi a6,zero,'\n'
	sb a1,0(a0)
	sb a2,0(a0)
	sb a3,0(a0)
	sb a4,0(a0)
	sb a5,0(a0)
	sb a6,0(a0)
	j _start
	/* set stack pointer */
	lui sp,0x80000000>>12
	# lui sp,(128*1024)>>12
	addi sp,sp,0x100

	/* call C code */
	jal ra,main

	j loop

.balign 16
reset_vec:
	j _start

loop:
	wfi
	j loop