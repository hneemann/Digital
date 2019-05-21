<?
    if (elem.Bits > 1) {
        generics[0] := "Bits";
        generics[1] := "Default";
        moduleName = format("%s_Nbit", moduleName);
        export bitRange := "[(Bits-1):0] ";
        export setExpr := "{Bits{1'b1}}";
    }
    else {
        generics[0] := "Default";
        moduleName = format("%s_1bit", moduleName);
        export bitRange := "";
        export setExpr := "1'b1";
    }
?>
module <?= moduleName ?>
#(<?
if (elem.Bits > 1) { ?>
    parameter Bits = 2,
<?- } ?>
    parameter Default = 0
)
(
   input Set,
   input <?= bitRange ?>D,
   input C,
   input Clr,
   output <?= bitRange ?>Q,
   output <?= bitRange ?>\~Q
);
    reg <?= bitRange ?>state;

    assign Q = state;
    assign \~Q  = ~state;

    always @ (posedge C or posedge Clr or posedge Set)
    begin
        if (Set)
            state <= <?= setExpr ?>;
        else if (Clr)
            state <= 'h0;
        else
            state <= D;
    end

    initial begin
        state = Default;
    end
endmodule