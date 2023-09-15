<?
    if (elem.Bits > 1) {
        generics[0] := "Bits";
        export bitRange := "[(Bits-1):0] ";
        export zval := "{Bits{1'bz}}";
    }
    else {
        moduleName = moduleName+"_BUS";
        export bitRange := "";
        export zval := "1'bz";
    }
?>module <?= moduleName ?><?
if (elem.Bits > 1) { ?>
#(
    parameter Bits = 2
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
