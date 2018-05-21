<?
generics[0] := "Default";

if (isset(elem.description)) {
?>/*
<?= elem.description ?>
*/
<?
}
?>module <?= elem.name ?>
#(
    parameter Default = 1'b0
)
(
   input PORT_Set,
   input PORT_J,
   input PORT_C,
   input PORT_K,
   input PORT_Clr,
   output PORT_Q,
   output PORT_notQ
);
    reg state;

    assign PORT_Q = state;
    assign PORT_notQ = ~state;

    always @ (posedge PORT_C or posedge PORT_Clr or posedge PORT_Set) begin
        if (PORT_Set)
            state <= 1'b1;
        else if (PORT_Clr)
            state <= 1'b0;
        else if (~PORT_J & PORT_K)
            state <= 1'b0;
        else if (PORT_J & ~PORT_K)
            state <= 1'b1;
        else if (PORT_J & PORT_K)
            state <= ~state;
    end

    initial begin
        state = Default;
    end
endmodule
