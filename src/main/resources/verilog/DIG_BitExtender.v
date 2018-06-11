<?
if (elem.inputBits > 1) {
    generics[0] := "inputBits";
    generics[1] := "outputBits";

    moduleName = "DIG_BitExtender";
-?>
module DIG_BitExtender #(
    parameter inputBits = 2,
    parameter outputBits = 4
)
(
    input [(inputBits-1):0] in,
    output [(outputBits - 1):0] out
);
    assign out = {{(outputBits - inputBits){in[inputBits - 1]}}, in};
endmodule
<?
} else {
    generics[0] := "outputBits";
    moduleName = "DIG_BitExtenderSingle";
-?>
module DIG_BitExtenderSingle #(
    parameter outputBits = 2
)
(
    input in,
    output [(outputBits - 1):0] out
);
    assign out = {outputBits{in}};
endmodule
<? } ?>

