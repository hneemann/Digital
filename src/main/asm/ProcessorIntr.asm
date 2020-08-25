        
        .const KEY 0x1e
        .const TERM 0x1f
        .const GPO 0x15

        jmp main

; interrupt service routine

        .org 2
        enteri

        in R0,KEY
        out TERM,R0

        leavei
        reti

; main programm

main:   ldi R0,1
L1:     out GPO, R0
        lsl R0
        brne L1

        ldi R0,0x8000
L2:     out GPO, R0
        lsr R0
        brne L2

        jmp main
