#ifndef __ADDRESS_H_
#define __ADDRESS_H_

#define ENABLE_CORES 0

#define BOOTADDR_REG 0x4000
#define FLASH_ADDR 0x20100000 // 1 MB for bitstream
#define DDR_ADDR 0x80000000
#define FLASH_SIZE (16 * 1024 * 1024) // 16 MiB
#define __STACKSIZE__ 0x1000
#define FLASH_DONE 0x80700000
#if(ENABLE_CORES)
#define sync .word 0x500f
#else
#define sync
#endif

#endif