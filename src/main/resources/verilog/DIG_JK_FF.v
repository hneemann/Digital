<?
generics[0] := "Default";

?>module <?= moduleName ?>
#(
    parameter Default = 1'b0
)
(
   input J,
   input C,
   input K,
   output Q,
   output \~Q
);
    reg state;

    assign Q = state;
    assign \~Q = ~state;

    always @ (posedge C) begin
        if (~J & K)
            state <= 1'b0;
         else if (J & ~K)
            state <= 1'b1;
         else if (J & K)
            state <= ~state;
    end

    initial begin
        state = Default;
    end
endmodule
