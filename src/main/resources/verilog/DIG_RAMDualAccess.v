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
    input PORT_C, // Clock signal
    input PORT_ld, 
    input [(AddrBits-1):0] PORT_1A,
    input [(AddrBits-1):0] PORT_2A,
    input [(Bits-1):0] PORT_1Din,
    input PORT_str,
    output [(Bits-1):0] PORT_1D,
    output [(Bits-1):0] PORT_2D
);
    reg [(Bits-1):0] memory [0:((1 << AddrBits)-1)];

    assign PORT_1D = PORT_ld? memory[PORT_1A] : 'hz;
    assign PORT_2D = memory[PORT_2A];
    
    always @ (posedge PORT_C) begin
        if (PORT_str)
            memory[PORT_1A] <= PORT_1Din;
    end
  
endmodule

