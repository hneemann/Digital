// SPDX-License-Identifier: AGPL-3.0-only
// SPDX-FileCopyrightText: Copyright 2026 Jonas Meeuws
#include <avr/io.h>
#include <stdint.h>
#include <stdio.h>
#include <string.h>

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
  // Set B(5) to high (Arduino Uno internal LED).
  DDRB  |= _BV(5);
  PORTB |= _BV(5);

  // Equivalent to setting the baud rate to F_CPU / 16.
  UBRR0H = UBRR0L = 0;

  fdev_setup_stream(&uart0, uart0_putc, NULL, _FDEV_SETUP_RW);
  stdout = &uart0;

  while (true)
    {
      fputs("Hello World!", stdout);
      fflush(stdout);
    }

  __builtin_unreachable();
}
