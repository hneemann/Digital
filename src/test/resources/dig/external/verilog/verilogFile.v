module add
(
    input [3:0] a,
    input [3:0] b,
    input c_i,
    output [3:0] s,
    output c_o
);
   wire [4:0] temp;

   assign temp = a + b + c_i;
   assign s = temp [3:0];
   assign c_o = temp[4];
endmodule