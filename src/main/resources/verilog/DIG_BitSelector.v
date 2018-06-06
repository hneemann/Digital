<?
    moduleName = "BitSel" + elem.'Selector Bits';
    Bits := 1 << elem.'Selector Bits';
    inRange := format("[%d:0]", Bits - 1);
    selRange := format("[%d:0]", elem.'Selector Bits' - 1);
?>
module <?= moduleName ?> (
    input <?= inRange ?> in,
    input <?= selRange ?> sel,
    output out
);
    assign out = in[sel];
endmodule
