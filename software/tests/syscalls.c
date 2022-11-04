// #include <limits.h>
#include <stdarg.h>
#include <stdint.h>
// #include <stdio.h>
#include <string.h>
#include <sys/signal.h>

#include "firmware.h"
#include "sifive-uart.h"

void write_uart(char* s, int len) {
  while (len--) {
    // *((char*)(UART_PORT)) = *(s++);
    sifive_uart_putc(*s);
    int delay = 0x1f;
    while (delay--)
      ;
  }
}

// size_t strlen(const char* s) {
//   const char* p = s;
//   while (*p) p++;
//   return p - s;
// }
// size_t strnlen(const char* s, size_t n) {
//   const char* p = s;
//   while (n-- && *p) p++;
//   return p - s;
// }

int putchar(int ch) {
  // static __thread char buf[64] __attribute__((aligned(64)));
  // static __thread int buflen = 0;

  // buf[buflen++] = ch;

  // if (ch == '\n' || buflen == sizeof(buf)) {
  //   write_uart(buf, buflen);
  //   buflen = 0;
  // }
  write_uart((char*)&ch, 1);

  return 0;
}

void printstr(char* s) { write_uart(s, strlen(s)); }

void printhex(uint64_t x) {
  char str[17];
  int i;
  for (i = 0; i < 16; i++) {
    str[15 - i] = (x & 0xF) + ((x & 0xF) < 10 ? '0' : 'a' - 10);
    x >>= 4;
  }
  str[16] = 0;

  printstr(str);
}

static inline void printnum(void (*putch)(int, void**), void** putdat,
                            unsigned long long num, unsigned base, int width,
                            int padc) {
  unsigned digs[sizeof(num) * 8];
  int pos = 0;

  while (1) {
    digs[pos++] = num % base;
    if (num < base) break;
    num /= base;
  }

  while (width-- > pos) putch(padc, putdat);

  while (pos-- > 0)
    putch(digs[pos] + (digs[pos] >= 10 ? 'a' - 10 : '0'), putdat);
}

static unsigned long long getuint(va_list* ap, int lflag) {
  if (lflag >= 2)
    return va_arg(*ap, unsigned long long);
  else if (lflag)
    return va_arg(*ap, unsigned long);
  else
    return va_arg(*ap, unsigned int);
}

static long long getint(va_list* ap, int lflag) {
  if (lflag >= 2)
    return va_arg(*ap, long long);
  else if (lflag)
    return va_arg(*ap, long);
  else
    return va_arg(*ap, int);
}

static void vprintfmt(void (*putch)(int, void**), void** putdat,
                      const char* fmt, va_list ap) {
  register const char* p;
  const char* last_fmt;
  register int ch, err;
  unsigned long long num;
  int base, lflag, width, precision, altflag;
  char padc;

  while (1) {
    while ((ch = *(unsigned char*)fmt) != '%') {
      if (ch == '\0') return;
      fmt++;
      putch(ch, putdat);
    }
    fmt++;

    // Process a %-escape sequence
    last_fmt = fmt;
    padc = ' ';
    width = -1;
    precision = -1;
    lflag = 0;
    altflag = 0;
  reswitch:
    switch (ch = *(unsigned char*)fmt++) {
      // flag to pad on the right
      case '-':
        padc = '-';
        goto reswitch;

      // flag to pad with 0's instead of spaces
      case '0':
        padc = '0';
        goto reswitch;

      // width field
      case '1':
      case '2':
      case '3':
      case '4':
      case '5':
      case '6':
      case '7':
      case '8':
      case '9':
        for (precision = 0;; ++fmt) {
          precision = precision * 10 + ch - '0';
          ch = *fmt;
          if (ch < '0' || ch > '9') break;
        }
        goto process_precision;

      case '*':
        precision = va_arg(ap, int);
        goto process_precision;

      case '.':
        if (width < 0) width = 0;
        goto reswitch;

      case '#':
        altflag = 1;
        goto reswitch;

      process_precision:
        if (width < 0) width = precision, precision = -1;
        goto reswitch;

      // long flag (doubled for long long)
      case 'l':
        lflag++;
        goto reswitch;

      // character
      case 'c':
        putch(va_arg(ap, int), putdat);
        break;

      // string
      case 's':
        if ((p = va_arg(ap, char*)) == NULL) p = "(null)";
        if (width > 0 && padc != '-')
          for (width -= strnlen(p, precision); width > 0; width--)
            putch(padc, putdat);
        for (; (ch = *p) != '\0' && (precision < 0 || --precision >= 0);
             width--) {
          putch(ch, putdat);
          p++;
        }
        for (; width > 0; width--) putch(' ', putdat);
        break;

      // (signed) decimal
      case 'd':
        num = getint(&ap, lflag);
        if ((long long)num < 0) {
          putch('-', putdat);
          num = -(long long)num;
        }
        base = 10;
        goto signed_number;

      // unsigned decimal
      case 'u':
        base = 10;
        goto unsigned_number;

      // (unsigned) octal
      case 'o':
        // should do something with padding so it's always 3 octits
        base = 8;
        goto unsigned_number;

      // pointer
      case 'p':
        // static_assert(sizeof(long) == sizeof(void*));
        lflag = 1;
        putch('0', putdat);
        putch('x', putdat);
        /* fall through to 'x' */

      // (unsigned) hexadecimal
      case 'X':
      case 'x':
        base = 16;
      unsigned_number:
        num = getuint(&ap, lflag);
      signed_number:
        printnum(putch, putdat, num, base, width, padc);
        break;

      // escaped '%' character
      case '%':
        putch(ch, putdat);
        break;

      // unrecognized escape sequence - just print it literally
      default:
        putch('%', putdat);
        fmt = last_fmt;
        break;
    }
  }
}

int printf(const char* fmt, ...) {
  // int len = strlen(fmt);
  // while (len--) {
  //   *((uint8_t*)(UART_PORT)) = *(fmt++);
  //   uint32_t delay = 0x1f;
  //   while (delay--)
  //     ;
  // }
  // return 0;
  va_list ap;
  va_start(ap, fmt);

  vprintfmt((void*)putchar, 0, fmt, ap);

  va_end(ap);
  return 0;  // incorrect return value, but who cares, anyway?
}

int puts(const char* s) {
  printf(s);
  return 0;
}

// #pragma GCC push_options
// #pragma GCC optimize("O0")
// void* memset(void* dest, int byte, size_t len) {
//   if ((((uintptr_t)dest | len) & (sizeof(uintptr_t) - 1)) == 0) {
//     uintptr_t word = byte & 0xFF;
//     word |= word << 8;
//     word |= word << 16;
//     word |= word << 16 << 16;

//     uintptr_t* d = dest;
//     while (d < (uintptr_t*)(dest + len)) *d++ = word;
//   } else {
//     char* d = dest;
//     while (d < (char*)(dest + len)) *d++ = byte;
//   }
//   return dest;
//   // uint8_t *p = dest;
//   // while(len--) {
//   //   *(p++) = byte;
//   // }
//   // return dest;
// }
// #pragma GCC pop_options

void handle_trap(size_t xcause, size_t xtval, size_t xepc, uint32_t* sp) {
  printf("trap! xcause = %x\n", xcause);
  // while (1) asm volatile("wfi");
  while (1)
    ;
}