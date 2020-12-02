; Conways Game of Life

	.const GRAPHIC 0x8000
	.const BANKSWITCH 0x7fff
	.const SIZE    20
	.const COLOR   3
	
	.reg r_pos R0 
	.reg w_pos R1 
	.reg x R2 
	.reg y R3 
	.reg TEMP R4 
	.reg R_OFFS R5 
	.reg W_OFFS R6
	.reg COUNT R7
	.reg VALUE R8
	.reg BANK R9
	.reg NEWVAL R10
	.reg COUNT_RET R11
	.reg COUNT_R R12

	ldi W_OFFS,GRAPHIC
	ldi R_OFFS,GRAPHIC+SIZE*SIZE
	ldi BANK,0

	;init screen
	ldi VALUE, COLOR

;	Pulsator
	ldi x, 5
	ldi y, 10
pul1:	call set
	inc x
	cpi x, 15
	brne pul1

	; start programm
	jmp SWAP_PAGE

	; calculate new generation
START:	mov r_pos,R_OFFS
	mov w_pos,W_OFFS
        ldi y,0
L_Y:	ldi x,0
L_X:	ldi NEWVAL,0
	rcall COUNT_R,count
	inr VALUE,[r_pos]
	cpi VALUE,0
	BREQ isDead
isAlive:
	cpi COUNT,2
	breq wake
	cpi COUNT,3
	breq wake
	jmp loopEnd
isDead:
	cpi COUNT,3
	brne loopEnd
wake:	ldi NEWVAL,COLOR
	
loopEnd: 
	outr [w_pos], NEWVAL
	inc r_pos
	inc w_pos
	inc x
	cpi x, SIZE
	brne L_X
	inc y
	cpi y, SIZE
	brne L_Y

	; make the new generation visible
	EORI BANK,1
	out BANKSWITCH,BANK
	
SWAP_PAGE:
	; swap page to read and page to write
	mov TEMP, W_OFFS
	mov W_OFFS, R_OFFS
	mov R_OFFS, TEMP

	brk
	jmp START
	

count:  ldi COUNT,0
	dec y
	subi r_pos,SIZE
	rcall COUNT_RET, count_check
	inc x
	addi r_pos,1
	rcall COUNT_RET, count_check
	inc y
	addi r_pos,SIZE
	rcall COUNT_RET, count_check
	inc y
	addi r_pos,SIZE
	rcall COUNT_RET, count_check
	dec x
	subi r_pos,1
	rcall COUNT_RET, count_check
	dec x
	subi r_pos,1
	rcall COUNT_RET, count_check
	dec y
	subi r_pos,SIZE
	rcall COUNT_RET, count_check
	dec y
	subi r_pos,SIZE
	rcall COUNT_RET, count_check
	inc x
	inc y
	addi r_pos,SIZE+1
	rret COUNT_R
count_check:
	cpi x,SIZE ; if running pulsator, border check is not necessary
	brcc c1
	cpi y,SIZE
	brcc c1
	inr VALUE,[r_pos]
	cpi VALUE,0
	breq c1
	inc COUNT
c1:	rret COUNT_RET


set:	mov TEMP,y
	muli TEMP,SIZE
	add TEMP,x
	add TEMP,W_OFFS
	outr [TEMP],VALUE
	ret

