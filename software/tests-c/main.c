#include "firmware.h"
#include "sifive-uart.h"

// #define CLIT_ADDR 0x2000000
#define BOOTADDR_REG 0x4000

void main(void) {
  uart_init();
  hello();
  print_str("jumping to BOOTADDR...\n");
  // (*(void (*)(void))(*((uint32_t*)(BOOTADDR_REG))))();
  // print_str("exec done\n");
}

void park(void) {
  asm("wfi");
  park();
}