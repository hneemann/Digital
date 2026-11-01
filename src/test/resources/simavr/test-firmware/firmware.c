// SPDX-License-Identifier: AGPL-3.0-only
// SPDX-FileCopyrightText: Copyright 2026 Jonas Meeuws
#include <avr/io.h>
#include <stdbool.h>
#include <stdio.h>
#include <string.h>

#define SETUP_INPUT_PIN(portid, bit) (DDR##portid &= ~_BV(bit))
#define SETUP_OUTPUT_PIN(portid, bit) (DDR##portid |= _BV(bit))
#define READ_PIN(portid, bit) (bit_is_set(PIN##portid, P##portid##bit) ? 1 : 0)
#define WRITE_PIN(portid, bit, value)           \
  (value                                        \
   ? (PORT##portid |= _BV(bit))                 \
   : (PORT##portid &= ~_BV(bit)))

static FILE uart0 = {};

static int
uart0_putc(char c,
           FILE *stream)
{
  (void)stream;
  while ((UCSR0A & _BV(UDRE0)) == 0) {}
  UDR0 = c;
  return 0;
}

int
main()
{
  // Set PB(5) to high (Arduino Uno D13/internal LED).
  SETUP_OUTPUT_PIN(B, 5);
  WRITE_PIN(B, 5, 1);

  // Setup uart0 (Arduino Uno D0/D1) as a pseudo stdout.
  UBRR0H = UBRR0L = 0; // Equivalent to setting the baud rate to F_CPU / 16.
  fdev_setup_stream(&uart0, uart0_putc, NULL, _FDEV_SETUP_RW);
  stdout = &uart0;

  // Setup PD(2) as input (Arduino Uno D2).
  SETUP_INPUT_PIN(D, 2);

  while (true)
    {
      fputs("Hello World!\n", stdout);
      fprintf(stdout, "PD2 is %u\n", READ_PIN(D, 2));
      fflush(stdout);
    }

  __builtin_unreachable();
}
