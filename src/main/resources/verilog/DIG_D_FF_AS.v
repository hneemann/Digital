<?
    if (elem.bits > 1) {
        generics[0] := "Bits";
        generics[1] := "Default";
        moduleName = format("%s_Nbit", elem.name);
        bitRange := "[(Bits-1):0] ";
        setExpr := "{Bits{1'b1}}";
    }
    else {
        generics[0] := "Default";
        moduleName = format("%s_1bit", elem.name);
        bitRange := "";
        setExpr := "1'b1";
    }
?>
module <?= moduleName ?>
#(<?
if (elem.bits > 1) { ?>
    parameter Bits = 2,<?
} ?>
    parameter Default = 0
)
(
   input PORT_Set,
   input <?= bitRange ?>PORT_D,
   input PORT_C,
   input PORT_Clr,
   output <?= bitRange ?>PORT_Q,
   output <?= bitRange ?>PORT_notQ
);
    reg <?= bitRange ?>state;

    assign PORT_Q = state;
    assign PORT_notQ = ~state;

    always @ (posedge PORT_C or posedge PORT_Clr or posedge PORT_Set)
    begin
        if (PORT_Set)
            state <= <?= setExpr ?>;
        else if (PORT_Clr)
            state <= 'h0;
        else
            state <= PORT_D;
    end

    initial begin
        state = Default;
    end
endmodule