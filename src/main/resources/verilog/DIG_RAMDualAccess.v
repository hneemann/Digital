<?
// Module instantiation parameters
generics[0] := "Bits";
generics[1] := "AddrBits";

?>module <?= moduleName ?>
#(
    parameter Bits = 8,
    parameter AddrBits = 4
)
(
    input C, // Clock signal
    input ld,
    input [(AddrBits-1):0] \1A ,
    input [(AddrBits-1):0] \2A ,
    input [(Bits-1):0] \1Din ,
    input str,
    output [(Bits-1):0] \1D ,
    output [(Bits-1):0] \2D
);
    // CAUTION: uses distributed RAM
    reg [(Bits-1):0] memory [0:((1 << AddrBits)-1)];

    assign \1D = ld? memory[\1A ] : 'hz;
    assign \2D = memory[\2A ];

    always @ (posedge C) begin
        if (str)
            memory[\1A ] <= \1Din ;
    end

endmodule

