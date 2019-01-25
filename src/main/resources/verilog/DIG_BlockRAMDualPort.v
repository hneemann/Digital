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
  input [(AddrBits-1):0] A,
  input [(Bits-1):0] Din,
  input str,
  input C,
  output [(Bits-1):0] D
);
  reg [(Bits-1):0] memory[0:((1 << AddrBits) - 1)];
  reg [(Bits-1):0] wData;

  assign D = wData;

  always @ (posedge C) begin
    wData <= memory[A];
    if (str)
      memory[A] <= Din;
  end
endmodule
