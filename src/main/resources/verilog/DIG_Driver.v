<?
    if (elem.Bits = 1) {
        moduleName = "DriverInv";
        bitRange := "";
    }
    else {
        generics[0] := "Bits";
        moduleName = "DriverInvBus";
        bitRange := format("[%d:0]", elem.Bits);
    }
?>
module <?= moduleName ?>
<?- if (elem.Bits > 1) { ?>#(
    parameter Bits = 2
)
<? } ?>
(
    input <?= bitRange ?>in,
    input sel,
    output <?= bitRange ?>out
);
    assign out = (sel == 1'b1)? in : <?= elem.Bits ?>'bz;
endmodule
