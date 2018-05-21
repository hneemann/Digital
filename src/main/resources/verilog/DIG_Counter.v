<?
    if (elem.bits > 1) {
        generics[0] := "Bits";
        moduleName = format("%s_Nbit", elem.name);
        bitRange := "[(Bits-1):0] ";
    }
    else {
        bitRange := "";
        moduleName = format("%s_1bit", elem.name);
    }
?>
module <?
printf("%s", moduleName);

if (elem.bits > 1) { ?>
#(
    parameter Bits = 2
)
<? } ?>(
    output <?= bitRange ?>PORT_out,
    output PORT_ovf,
    input PORT_C,
    input PORT_en,
    input PORT_clr
);
    reg <?= bitRange ?>count;

    always @ (posedge PORT_C) begin
        if (PORT_clr)
          count <= 'h0;
        else if (PORT_en)
          count <= count + 1'b1;
    end

    assign PORT_out = count;
    assign PORT_ovf = PORT_en? &count : 1'b0;

    initial begin
        count = 'h0;
    end
endmodule
