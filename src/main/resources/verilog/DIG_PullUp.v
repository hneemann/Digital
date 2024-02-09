<?
    if (elem.Bits > 1) {
        generics[0] := "Bits";
        export bitRange := "[(Bits-1):0] ";
        export pullRange := "p[(Bits-1):0]";
    }
    else {
        moduleName = moduleName+"_BUS";
        export bitRange := "";
        export pullRange := "";
    }
?>module <?= moduleName ?><?
if (elem.Bits > 1) { ?>
#(
    parameter Bits = 2
)<? } ?>(output <?= bitRange ?>out);
pullup <?= pullRange ?>(out);
endmodule