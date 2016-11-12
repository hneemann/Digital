Start:
     LDI R0,15          ; load 15
     PUSH R0            ; put 15 an stack
     CALL fibonacci     ; call function
     STS 0, R0          ; store result
     BRK
     JMP Start

     .const n 2         ; offset of argument
     .const nm1 -1      ; offset of local var
fibonacci:
     ENTER 1            ; one local var needed

     LDD R0,[BP+n]      ; load n
     CPI R0,2           ; compare with 2
     BRCS fibEnd        ; if lower we are ready

     SUBI R0, 1         ; subtract one
     PUSH R0            ; push on stack
     CALL fibonacci     ; call recursive
     STD [BP+nm1],R0    ; store result in local var

     LDD R0,[BP+n]      ; load n again
     SUBI R0, 2         ; subtract 2
     PUSH R0            ; push on stack
     CALL fibonacci     ; call recursive
        
     LDD R1,[BP+nm1]    ; load local var f(n-1)
     ADD R0,R1          ; add f(n-1)+f(n-2)

fibEnd:
     LEAVE
     RET 1              ; remove argument