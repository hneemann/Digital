<?
    if (elem.Bits > 1) {
        generics[0] := "Bits";
        moduleName = format("%s_Nbit", moduleName);
        export bitRange := "[(Bits-1):0] ";
    }
    else {
        export bitRange := "";
        moduleName = format("%s_1bit", moduleName);
    }
?>
module <?
printf("%s", moduleName);

if (elem.Bits > 1) { ?>
#(
    parameter Bits = 2
)
<? } ?>(
    output <?= bitRange ?>out,
    output ovf,
    input C,
    input en,
    input clr
);
    reg <?= bitRange ?>count;

    always @ (posedge C) begin
        if (clr)
          count <= 'h0;
        else if (en)
          count <= count + 1'b1;
    end

    assign out = count;
    assign ovf = en? &count : 1'b0;

    initial begin
        count = 'h0;
    end
endmodule
