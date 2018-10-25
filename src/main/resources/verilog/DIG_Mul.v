<?

    if (elem.Signed)
       moduleName="DIG_Mul_signed";
    else
       moduleName="DIG_Mul_unsigned";

    generics[0] := "Bits";
?>
module <?= moduleName ?> #(
    parameter Bits = 1
)
(
    <? if (elem.Signed) {?>
      input signed [(Bits-1):0] a,
      input signed [(Bits-1):0] b,
      output signed [(Bits*2-1):0] mul
    <? } else { ?>
      input [(Bits-1):0] a,
      input [(Bits-1):0] b,
      output [(Bits*2-1):0] mul
    <? } ?>
);
    assign mul = a * b;
endmodule
