

	.reg x R0
	.reg y R1
	.reg char R2
	.reg addr R3
	.reg readChar R4
	.reg t0 R10
	.reg t1 R11
	.reg t2 R12

	.data text "Hello World!",0
	.word lastKeys

	ldi x, 40
	ldi y, 15
	ldi addr,text
text1:	ld char,[addr]
	cpi char,0
	breq textEnd
	rcall RA, setChar
	inc addr
	inc x
	jmp text1
	

textEnd:

; horizontal line

	ldi char, 0x30cd
	ldi x,1
frame1:	ldi y,0
	rcall RA, setChar
	ldi y,29
	rcall RA, setChar
	inc x
	cpi x,80
	brne frame1

; vertical line

	ldi char, 0x30ba
	ldi y,1
frame2:	ldi x,0
	rcall RA, setChar
	ldi x,79
	rcall RA, setChar
	inc y
	cpi y,30
	brne frame2

;corner

	ldi char,0x30c9	
	ldi x,0	
	ldi y,0	
	rcall RA, setChar
	ldi char,0x30bb
	ldi x,79	
	ldi y,0	
	rcall RA, setChar
	ldi char,0x30c8
	ldi x,0	
	ldi y,29	
	rcall RA, setChar
	ldi char,0x30bc
	ldi x,79	
	ldi y,29	
	rcall RA, setChar
	


; char table

	ldi char,0xf000
	ldi y,1

outer:	ldi x,1
inner:	rcall RA, setChar
	inc char
	inc x
	cpi x, 17
	brne inner
	inc y
	cpi y, 17
	brne outer

	brk

	ldi y,15
	ldi x,39

wa1:	ldi char, 0x2002
	rcall ra, readReplaceChar
	push readChar

	rcall ra, wait
	;brk

	pop char
	rcall ra, setChar


	in t0, 0x14       ; read keys
        lds t1, lastKeys
	;cmp t0,t1
	;breq wa1
	sts lastKeys, t0

	mov t1,t0
	andi t1,1
	breq notUp
	cpi y,1
	breq notUp
        dec y

notUp:
	mov t1,t0
	andi t1,2
	breq notLeft
	cpi x,1
	breq notLeft
        dec x


notLeft:
	mov t1,t0
	andi t1,4
	breq notRight
	cpi x,78
	breq notRight
        inc x


notRight:
	mov t1,t0
	andi t1,8
	breq wa1
	cpi y,28
	breq wa1
        inc y
	jmp wa1


wait:
	ldi t1,5  ; 38=200ms
	ldi t0,0
w1:	dec t0
	brne w1
	dec t1
	brne w1
	rret ra

readReplaceChar:
	mov t0,y
	swap t0
	lsr t0
	or t0,x
	addi t0,0x8000
	inr readChar, [t0]
	outr [t0], char
	rret RA



setChar:
	mov t0,y
	swap t0
	lsr t0
	or t0,x
	addi t0,0x8000
	outr [t0], char
	rret RA


