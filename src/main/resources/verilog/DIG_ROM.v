<?
    romSize = 1 << elem.addr_bits;
    moduleName = format("%s_%dX%d", elem.name, romSize, elem.bits);
    dBitRange = format("[%d:0]", elem.bits - 1);
    aBitRange = format("[%d:0]", elem.addr_bits - 1);

?>module <?= moduleName ?> (
    input <?= aBitRange ?> PORT_A,
    input PORT_sel,
    output <?= dBitRange ?> PORT_D
);
    reg <?= dBitRange ?> my_rom [0:<?= (romSize - 1) ?>];
    reg PORT_D;

    always @ (PORT_A or PORT_sel) begin
        if (~PORT_sel)
            PORT_D = <?= elem.bits ?>'hz;<?
        if (elem.data.size < romSize) { 
            lastAddr = format("%d'h%x", elem.addr_bits, elem.data.size - 1); ?>
        else if (PORT_A > <?= lastAddr ?>)
            PORT_D = <?= elem.bits ?>'h0;<?
        } ?>
        else
            PORT_D = my_rom[PORT_A];
    end

    initial begin<?

    for (i = 0; i < elem.data.size; i++) { ?>
        my_rom[<?= i ?>] = <?= format("%d'h%x", elem.bits, elem.data[i]) ?>;<?
    } ?>
    end
endmodule
