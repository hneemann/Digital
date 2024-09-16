<?
    if (elem.Label="")
        panic("err_romNeedsALabelToBeExported");

    romMaxSize := 1 << elem.AddrBits;
    data:=elem.Data;
    if (elem.autoReload) {
        data=loadHex(elem.lastDataFile, elem.Bits);
    }
    romSize := sizeOf(data);
    if (romSize = 0) {
        romSize = 1;
    }
    moduleName = format("%s_%dX%d_%s", moduleName, romMaxSize, elem.Bits, identifier(elem.Label));
    dBitRange := format("[%d:0]", elem.Bits - 1);
    aBitRange := format("[%d:0]", elem.AddrBits - 1);

?>module <?= moduleName ?> (
    input <?= aBitRange ?> A1,
    input <?= aBitRange ?> A2,
    input s1,
    input s2,
    output reg <?= dBitRange ?> D1,
    output reg <?= dBitRange ?> D2
);
    reg <?= dBitRange ?> my_rom [0:<?= (romSize - 1) ?>];

    always @ (*) begin
        if (~s1)
            D1 = <?= elem.Bits ?>'hz;<?
        if (romSize < romMaxSize) {
            lastAddr := format("%d'h%x", elem.AddrBits, romSize - 1); ?>
        else if (A1 > <?= lastAddr ?>)
            D1 = <?= elem.Bits ?>'h0;<?
        } ?>
        else
            D1 = my_rom[A1];
    end

    always @ (*) begin
        if (~s2)
            D2 = <?= elem.Bits ?>'hz;<?
        if (romSize < romMaxSize) {
            lastAddr := format("%d'h%x", elem.AddrBits, romSize - 1); ?>
        else if (A2 > <?= lastAddr ?>)
            D2 = <?= elem.Bits ?>'h0;<?
        } ?>
        else
            D2 = my_rom[A2];
    end

    initial begin<?
    if( sizeOf(data) = 0) { ?>
        my_rom[0]=0;<?
    }
    else {
        for (i := 0; i < romSize; i++) { ?>
        my_rom[<?= i ?>] = <?= format("%d'h%x", elem.Bits, data[i]) ?>;<?
        }
    } ?>
    end
endmodule
