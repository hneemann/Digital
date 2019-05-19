<?
    if (elem.Label="")
        panic("err_lutNeedsALabelToBeExported");

    lutSize := 1 << elem.Inputs;
    moduleName = format("LUT_%s", elem.Label);
    if (elem.Bits > 1)
        export dBitRange := format(" [%d:0] ", elem.Bits - 1);
    else
        export dBitRange := " ";

?>module <?= moduleName ?> (
<?- for (i:=0;i<elem.Inputs;i++) {?>
    input \<?=i?> ,
<?- }?>
    output reg <?= dBitRange ?> out
);
    reg<?= dBitRange ?>my_lut [0:<?= (lutSize - 1) ?>];
    wire [<?=elem.Inputs-1?>:0] temp;
    assign temp = {<?
        for (i:=elem.Inputs-1;i>=0;i--) {
            if (i<elem.Inputs-1) {
                print(" , ");
            }
            print("\\"+i);
        }
    ?> };

    always @ (*) begin
       out = my_lut[temp];
    end

    initial begin<?

    data := 0;
    for (i := 0; i < lutSize; i++) { ?>
        my_lut[<?= i ?>] = <?
        if (i<sizeOf(elem.Data)) {
            data = elem.Data[i];
        } else {
            data = 0;
        }
        print(format("%d'h%x", elem.Bits, data));
?>;<?
    } ?>
    end
endmodule
