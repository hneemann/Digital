<?
    generics[0] := "invertOutput";
?>
module <?= moduleName ?>
#(
    parameter invertOutput = 0
)
(
    output Reset
);
    // ToDo: how to deal with the reset pin?
    assign Reset = invertOutput;
endmodule