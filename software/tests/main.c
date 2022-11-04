#include "firmware.h"
#include "sifive-uart.h"

void main(void) {
  uart_init();
  hello();
}

void park(void) {
  asm("wfi");
  park();
}