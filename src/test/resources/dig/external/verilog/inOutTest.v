module test(inout wire out, input wire write, input wire read);
reg data=1;
always @(posedge write)
	begin
		data=out;
	end
assign out=read?data:'hz;
endmodule
