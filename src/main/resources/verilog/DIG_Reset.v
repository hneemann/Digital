<?
    generics[0] := "invertOutput";
?>
module <?= elem.name ?>
#(
    parameter invertOutput = 0
)
(
    output PORT_Reset
);
    // ToDo: how to deal with the reset pin?
    assign PORT_Reset = invertOutput;
endmodule