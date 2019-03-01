<?
    if (elem.Label="")
        panic("err_lutNeedsALabelToBeExported");

    lutSize := 1 << elem.Inputs;
    moduleName = format("LUT_%s", elem.Label);
    dBitRange := format("[%d:0]", elem.Bits - 1);

?>module <?= moduleName ?> (
<?- for (i:=0;i<elem.Inputs;i++) {?>
    input \<?=i?> ,
<?- }?>
    output reg <?= dBitRange ?> out
);
    reg <?= dBitRange ?> my_lut [0:<?= (lutSize - 1) ?>];
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

    for (i := 0; i < lutSize; i++) { ?>
        my_lut[<?= i ?>] = <?= format("%d'h%x", elem.Bits, elem.Data[i]) ?>;<?
    } ?>
    end
endmodule
