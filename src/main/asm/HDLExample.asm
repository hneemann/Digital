;Simple programm to show different light
;patterns.
;Is used in the VHDL processor example.

;Buttons:
; left:  right to left
; right: left to right
; down:  four lights
; up:    back and fourth
;
	.const GPO_PORT 0x5
	.const GPI_PORT 0x0 
	.const UP_MASK     0b0001
	.const LEFT_MASK   0b0010
	.const RIGHT_MASK  0b0100
	.const DOWN_MASK   0b1000

	.reg STATEADR R2
	
	.word lastButtons

	ldi STATEADR, state3

checkButton:
	in R0, GPI_PORT		; load button
	lds R1, lastButtons	; load last buttons
	sts lastButtons, R0
	cpi R0,0		; if buttons 0: donw
	breq none
	cpi R1,0		; if lastButtons!=0: done
	brne none

	mov R1,R0		; check buttons
	andi R1, UP_MASK
	brne state3		
	mov R1,R0
	andi R1, DOWN_MASK
	brne state4
	mov R1,R0
	andi R1, LEFT_MASK
	brne state1
	mov R1,R0
	andi R1, RIGHT_MASK
	brne state2
none:	rret STATEADR


	.reg counter R8
	.reg temp1   R9
	.reg dir     R10

state1:	ldi STATEADR, loop1
	ldi counter, 16
        ldi temp1,1 
loop1:	OUT GPO_PORT, temp1
	LSL temp1
	DEC counter
	BRNE st1_2
	ldi counter, 16
        ldi temp1,1 
st1_2:	jmp checkButton


state2:	ldi STATEADR, loop2
	ldi counter, 16
        ldi temp1,0x8000 
loop2:	OUT GPO_PORT, temp1
	LSR temp1
	DEC counter
	BRNE st2_2
	ldi counter, 16
        ldi temp1,0x8000 
st2_2:	jmp checkButton

state3:	ldi STATEADR, loop3
        ldi temp1,1
	ldi dir,0
loop3:	OUT GPO_PORT, temp1
	cpi dir,0
	brne st3_0
	LSL temp1
	cpi temp1, 0x8000
        brne checkButton
	ldi dir,1
	jmp checkButton
st3_0:	LSR temp1
	cpi temp1, 1
        brne checkButton
	ldi dir,0
	jmp checkButton


state4:	ldi STATEADR, loop4
	ldi counter, 4
        ldi temp1,0x1111 
loop4:	OUT GPO_PORT, temp1
	LSL temp1
	DEC counter
	BRNE st1_4
	ldi counter, 4
        ldi temp1,0x1111 
st1_4:	jmp checkButton

