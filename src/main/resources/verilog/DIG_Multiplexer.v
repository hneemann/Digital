<?
    if (elem.Bits = 1) {
        moduleName = format("Mux_%dx1", 1 << elem.'Selector Bits');
        export bitRange := "";
    }
    else {
        generics[0] := "Bits";
        moduleName = format("Mux_%dx1_NBits", 1 << elem.'Selector Bits');
        export bitRange := "[(Bits - 1):0] ";
    }

    selRange := format("[%d:0] ", elem.'Selector Bits' - 1);
    inCount := 1 << elem.'Selector Bits';
?>
module <?= moduleName ?>
<?- if (elem.Bits > 1) { ?> #(
    parameter Bits = 2
)
<?- } ?>
(
    input <?= selRange ?>sel,
    <? for (n:=0; n < inCount; n++) { -?>
    input <?= bitRange ?>in_<?= n ?>,
    <? } -?>
    output reg <?= bitRange ?>out
);
    always @ (*) begin
        case (sel)
    <?- for (n:=0; n < inCount; n++) { ?>
            <?= elem.'Selector Bits' ?>'h<?= format("%x", n) ?>: out = in_<?= n ?>;
    <?- } ?>
            default:
                out = 'h0;
        endcase
    end
endmodule
