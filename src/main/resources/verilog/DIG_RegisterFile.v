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
    input [(Bits-1):0] Din,
    input we,
    input [(AddrBits-1):0] Rw,
    input C,
    input [(AddrBits-1):0] Ra,
    input [(AddrBits-1):0] Rb,
    output [(Bits-1):0] Da,
    output [(Bits-1):0] Db
);

    reg [(Bits-1):0] memory[0:((1 << AddrBits)-1)];

    assign Da = memory[Ra];
    assign Db = memory[Rb];

    always @ (posedge C) begin
        if (we)
            memory[Rw] <= Din;
    end
endmodule
