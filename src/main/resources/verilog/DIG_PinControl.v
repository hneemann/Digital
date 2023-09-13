<?
    if (elem.Bits > 1) {
        export bitRange := "[(Bits-1):0] ";
    }
    else {
        export bitRange := "";
    }
    export zval := elem.Bits+"'bz";
?>module <?= moduleName ?><?
if (elem.Bits > 1) { ?>
#(
    parameter Bits = <?=elem.Bits?>
)
<? } ?>(
   inout <?= bitRange ?>pin,
   input oe,
   input <?= bitRange ?>wr,
   output <?= bitRange ?>rd
);

  assign pin = oe ? wr : <?= zval ?>;
  assign rd  = oe ? wr : pin ;
endmodule
