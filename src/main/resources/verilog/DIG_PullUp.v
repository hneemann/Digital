<?
    if (elem.Bits > 1) {
        export bitRange := "[(Bits-1):0] ";
    }
    else {
        export bitRange := "";
    }
?>module <?= moduleName ?><?
                          if (elem.Bits > 1) { ?>
                          #(
                              parameter Bits = <?=elem.Bits?>
                          )
                          <? } ?>(output <?= bitRange ?>out);
pullup p<?= bitRange ?>(out);
endmodule