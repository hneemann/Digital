<?
    if (elem.Bits > 1) {
        generics[0] := "Bits";
        generics[1] := "Default";
        moduleName = format("%s_Nbit", moduleName);
        export bitRange := "[(Bits-1):0] ";
    }
    else {
        generics[0] := "Default";
        moduleName = format("%s_1bit", moduleName);
        export bitRange := "";
    }
?>module <?= moduleName ?>
#(<?
if (elem.Bits > 1) { ?>
    parameter Bits = 2,<?
} ?>
    parameter Default = 0
)
(
   input <?= bitRange ?>D,
   input C,
   output <?= bitRange ?>Q,
   output <?= bitRange ?>\~Q
);
    reg <?= bitRange ?>state;

    assign Q = state;
    assign \~Q = ~state;

    always @ (posedge C) begin
        state <= D;
    end

    initial begin
        state = Default;
    end
endmodule
