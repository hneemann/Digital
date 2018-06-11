<?
    if (elem.Signed)
        moduleName = "CompSigned";
    else
        moduleName = "CompUnsigned";

    generics[0] := "Bits";
?>
module <?= moduleName ?> #(
    parameter Bits = 1
)
(
    input [(Bits -1):0] a,
    input [(Bits -1):0] b,
    output \> ,
    output \= ,
    output \<
);
<?- if (elem.Signed) { ?>
    assign \> = $signed(a) > $signed(b);
    assign \= = $signed(a) == $signed(b);
    assign \< = $signed(a) < $signed(b);
<?- } else { ?>
    assign \> = a > b;
    assign \= = a == b;
    assign \< = a < b;
<?- } ?>
endmodule
