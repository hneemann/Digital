#!/bin/bash
inkscape $1.svg -w 28 -C --export-filename=../resources/icons/$1.png
inkscape $1.svg -w 56 -C --export-filename=../resources/icons/$1_hi.png
