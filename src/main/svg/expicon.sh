#!/bin/bash
inkscape $1.svg -w 28 -C -e ../resources/$1.png
inkscape $1.svg -w 56 -C -e ../resources/$1_hi.png
