<?
    if (elem.Bits=1)
        moduleName = "Demux" + elem.'Selector Bits';
    else
        moduleName = "DemuxBus" + elem.'Selector Bits';

    outCount := 1<<elem.'Selector Bits';
    bitRange := "";
    if (elem.Bits>1)
        bitRange="[(Bits-1):0] ";

    selRange := format("[%d:0]", elem.'Selector Bits' - 1);
?>
module <?= moduleName ?>
#(<?- if (elem.Bits > 1) {
    generics[0] := "Bits"; ?>
    parameter Bits = 2,
<?- } ?>
    parameter Default = 0 <? if (elem.Bits > 1) { generics[1] := "Default";} else {generics[0] := "Default";} ?>
)
(
    <?- for (i:=0; i<outCount; i++) { ?>
    output <?= bitRange ?>out_<?=i?>,
    <?- } ?>
    input <?= selRange ?> sel,
    input <?= bitRange ?>in
);
<?- for (i:=0; i<outCount; i++) {?>
    assign out_<?= i ?> = (sel == <?= format("%d'h%x", elem.'Selector Bits', i) ?>)? in : Default;
<?- } ?>
endmodule
