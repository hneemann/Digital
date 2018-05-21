<?
    if (elem.bits > 1) {
        generics[0] := "Bits";
        generics[1] := "Default";
        moduleName = format("%s_Nbit", elem.name);
        bitRange := "[(Bits-1):0] ";
    }
    else {
        generics[0] := "Default";
        moduleName = format("%s_1bit", elem.name);
        bitRange := "";
    }
?>module <?= moduleName ?>
#(<?
if (elem.bits > 1) { ?>
    parameter Bits = 2,<?
} ?>
    parameter Default = 0
)
(
   input <?= bitRange ?>PORT_D,
   input PORT_C,
   output <?= bitRange ?>PORT_Q,
   output <?= bitRange ?>PORT_notQ
);
    reg <?= bitRange ?>state;

    assign PORT_Q = state;
    assign PORT_notQ = ~state;

    always @ (posedge PORT_C) begin
        state <= PORT_D;
    end

    initial begin
        state = Default;
    end
endmodule
