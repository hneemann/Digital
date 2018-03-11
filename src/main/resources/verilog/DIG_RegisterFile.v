<?
// Module instantiation parameters
generics[0] = "Bits";
generics[1] = "AddrBits";

?>module <?= elem.name ?>
#(
    parameter Bits = 8,
    parameter AddrBits = 4
)
(
    input [(Bits-1):0] PORT_Din,
    input PORT_we,
    input [(AddrBits-1):0] PORT_Rw,
    input PORT_C,
    input [(AddrBits-1):0] PORT_Ra,
    input [(AddrBits-1):0] PORT_Rb,
    output [(Bits-1):0] PORT_Da,
    output [(Bits-1):0] PORT_Db
);

    reg [(Bits-1):0] memory[0:((1 << AddrBits)-1)];
    
    assign PORT_Da = memory[PORT_Ra];
    assign PORT_Db = memory[PORT_Rb];
    
    always @ (posedge PORT_C) begin
        if (PORT_we)
            memory[PORT_Rw] <= PORT_Din;
    end
endmodule
