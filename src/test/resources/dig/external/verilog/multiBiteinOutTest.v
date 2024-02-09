module test(inout wire [7:0]out, input wire write, input wire read);
reg [7:0] data='hFF;
always @(posedge write)
	begin
		data=out;
	end
assign out=read?data:'hz;
endmodule
