<?
    generics[0] := "Bits";
?>
module DIG_Neg #(
    parameter Bits = 1
)
(
      input signed [(Bits-1):0] in,
      output signed [(Bits-1):0] out
);
    assign out = -in;
endmodule
