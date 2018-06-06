<?
    moduleName = "Decoder" + elem.'Selector Bits';
    outCount := 1 << elem.'Selector Bits';
    selRange := format("[%d:0]", elem.'Selector Bits' - 1);
?>
module <?= moduleName ?> (
    <?- for (i:=0; i<outCount; i++) {?>
    output out_<?= i ?>,
    <?- } ?>
    input <?= selRange ?> sel
);
<?- for (i:=0; i<outCount; i++) {?>
    assign out_<?= i ?> = (sel == <?= format("%d'h%x", elem.'Selector Bits', i) ?>)? 1'b1 : 1'b0;
<?- } ?>
endmodule
