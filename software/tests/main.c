#include "firmware.h"
#include "sifive-uart.h"

#define CLIT_ADDR 0x2000000

void main(void) {
  uart_init();
  hello();
  print_str("jumping to CLINT...\n");
  (*(void (*)(void))(*((uint32_t*)(0x2000000))))();
  print_str("exec done\n");
}

void park(void) {
  asm("wfi");
  park();
}