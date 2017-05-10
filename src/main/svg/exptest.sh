#!/bin/bash
inkscape $1.svg -w 16 -C -e ../resources/$1.png
inkscape $1.svg -w 32 -C -e ../resources/$1_hi.png
