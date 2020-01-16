<?
  if (elem.Bits = 1) {
    moduleName = "DIG_Regs";
    bitRange := "";
  } else {
    moduleName = "DIG_Regs_BUS";
    generics[0] := "Bits";
    bitRange := "[(Bits - 1):0]";
  }
?>

module <?= moduleName ?>
<?- if (elem.Bits > 1) { ?> #(
  parameter Bits = 1
)
<?- } ?>
(
  input C,
  input en,
  input <?= bitRange ?> D,
  output <?= bitRange ?> Q
);

reg <?= bitRange ?> state = 'h0;

assign Q = state;

always @ (posedge C) begin
  if (en)
    state <= D;
end
always @ (negedge C) begin
  if (en)
    state <= 1'b0;
end

endmodule