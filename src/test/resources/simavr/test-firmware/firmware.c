// SPDX-License-Identifier: AGPL-3.0-only
// SPDX-FileCopyrightText: Copyright 2026 Jonas Meeuws
#include <avr/io.h>
#include <avr/sleep.h>
#include <stdint.h>

int
main()
{
  DDRB = 0x00;
  DDRD = 0xFF;
  while (1)
    PORTD = PINB;

  sleep_mode();
  __builtin_unreachable();
}
