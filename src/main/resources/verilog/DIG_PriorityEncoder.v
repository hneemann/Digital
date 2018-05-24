<?
    moduleName = "PriorityEncoder" + elem.'Selector Bits';
    inCount := 1 << elem.'Selector Bits';
    selRange := format("[%d:0]", elem.'Selector Bits' - 1);
-?>
module <?= moduleName ?> (
    <?- for (n:=0; n<inCount; n++) { ?>
    input in<?= n ?>,
    <?- } ?>
    output reg <?= selRange ?> num,
    output any
);
    always @ (*) begin
        <? for (n:=inCount-1; n>0; n--) { -?>
        if (in<?= n ?> == 1'b1)
            num = <?= format("%d'h%x", elem.'Selector Bits', n) ?>;
        else<?= " " ?>
        <?- } ?>
            num = <?= format("%d'h0", elem.'Selector Bits') ?>;
    end

    assign any = <?
      for (n:=0; n<inCount; n++) {
        print("in", n);
        if (n < inCount-1) print(" | ");
      }?>;
endmodule
