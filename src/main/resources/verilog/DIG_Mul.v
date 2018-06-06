<?
    generics[0] := "Bits";
?>
module DIG_Mul #(
    parameter Bits = 1
)
(
    input [(Bits-1):0] a,
    input [(Bits-1):0] b,
    output [(Bits*2-1):0] mul
);
    assign mul = a * b;
endmodule
