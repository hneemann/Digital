<?
    generics[0] := "maxCounter";
?>
module DIG_simpleClockDivider
#(
    parameter maxCounter = 1
)
(
    input PORT_in,
    output PORT_out
);
 /*
  * Don't use a logic signal as clock source in a real world application!
  * Use the on chip clock resources instead!
  */
  reg [31:0] counter;
  reg state;

  assign PORT_out = state;

  always @ (posedge PORT_in) begin
       if (counter == maxCounter) begin
            counter <= 0;
            state <= ~state;
       end
       else begin
          counter <= counter + 1;
       end
  end

endmodule
