# SPDX-License-Identifier: AGPL-3.0-only
# SPDX-FileCopyrightText: Copyright 2026 Jonas Meeuws

CC = avr-gcc
CFLAGS =

all: firmware.elf PHONY

firmware.elf: firmware.c
	${CC} \
	  -mmcu=atmega328p \
	  -DF_CPU=16000000 \
	  -std=gnu17 \
	  -Wall \
	  -Wextra \
	  -Os \
	  ${CFLAGS} \
	  -o $@ \
	  $<

clean: PHONY
	rm -f *.elf

.DELETE_ON_ERROR:
.PHONY: PHONY
