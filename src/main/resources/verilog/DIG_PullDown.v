<?
    if (elem.Bits > 1) {
        export bitRange := "[(Bits-1):0] ";
    }
    else {
        export bitRange := "";
    }
?>module <?= moduleName ?>(output <?= bitRange ?>out);
pulldown(out);
endmodule