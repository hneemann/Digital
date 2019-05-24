<?
    if (elem.Bits = 1) {
        moduleName = "DIG_Register";
        export bitRange := "";
    }
    else {
        moduleName = "DIG_Register_BUS";
        generics[0] := "Bits";
        export bitRange := "[(Bits - 1):0]";
    }
?>
module <?= moduleName ?>
<?- if (elem.Bits > 1) {?> #(
    parameter Bits = 1
)
<?- } ?>
(
    input C,
    input en,
    input <?= bitRange ?>D,
    output <?= bitRange ?>Q
);

    reg <?= bitRange ?> state = 'h0;

    assign Q = state;

    always @ (posedge C) begin
        if (en)
            state <= D;
   end
endmodule