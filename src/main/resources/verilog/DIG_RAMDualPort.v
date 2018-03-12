<?
// Module instantiation parameters
generics[0] = "Bits";
generics[1] = "AddrBits";

?>module <?= elem.name ?>
#(
    parameter Bits = 8,
    parameter AddrBits = 4
)
(
  input [(AddrBits-1):0] PORT_A,
  input [(Bits-1):0] PORT_Din,
  input PORT_str,
  input PORT_C,
  input PORT_ld,
  output [(Bits-1):0] PORT_D
);
  reg [(Bits-1):0]  memory[0:((1 << AddrBits) - 1)];

  assign PORT_D = PORT_ld? memory[PORT_A] : 'hz;

  always @ (posedge PORT_C) begin
    if (PORT_str)
      memory[PORT_A] <= PORT_Din;
  end
endmodule
