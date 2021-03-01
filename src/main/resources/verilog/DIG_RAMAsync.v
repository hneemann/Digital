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
  input [(Bits-1):0] D,
  input we,
  output [(Bits-1):0] Q
);
  reg [(Bits-1):0] memory[0:((1 << AddrBits) - 1)];

  assign Q = memory[A];

  always @ (we, A, D) begin
    if (we)
      memory[A] <= D;
  end
endmodule
