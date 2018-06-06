<?
    if (elem.Bits = 1) {
        moduleName = "DriverInv";
        bitRange := "";
        zval := "1'bz";
    }
    else {
        generics[0] := "Bits";
        moduleName = "DriverInvBus";
        bitRange := "[(Bits-1):0] ";
        zval := "{Bits{1'bz}}";
    }
?>
module <?= moduleName ?>
<?- if (elem.Bits > 1) { ?>#(
    parameter Bits = 2
)
<?- } ?>
(
    input <?= bitRange ?>in,
    input sel,
    output <?= bitRange ?>out
);
    assign out = (sel == 1'b0)? in : <?= zval ?>;
endmodule