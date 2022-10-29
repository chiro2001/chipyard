#include "firmware.h"

void hello(void) {
  print_str("Hello, UART@0x");
  print_hex(UART_PORT, 8);
  print_str("\n");
}