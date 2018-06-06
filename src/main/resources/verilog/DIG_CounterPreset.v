<?
    if (elem.Bits<2)
      panic("err_counterNeedsMoreBits");

    generics[0] := "Bits";
    generics[1] := "maxValue";
?>
module DIG_CounterPreset #(
    parameter Bits = 2,
    parameter maxValue = 4
)
(
    input C,
    input en,
    input clr,
    input dir,
    input [(Bits-1):0] in,
    input ld,
    output [(Bits-1):0] out,
    output ovf
);

    reg [(Bits-1):0] count = 'h0;

    function [(Bits-1):0] maxVal (input [(Bits-1):0] maxv);
        if (maxv == 0)
            maxVal = (1 << Bits) - 1;
        else
            maxVal = maxv;
    endfunction

    assign out = count;
    assign ovf = ((count == maxVal(maxValue) & dir == 1'b0)
                  | (count == 'b0 & dir == 1'b1))? en : 1'b0;

    always @ (posedge C) begin
        if (clr == 1'b1)
            count <= 'h0;
        else if (ld == 1'b1)
            count <= in;
        else if (en == 1'b1) begin
            if (dir == 1'b0) begin
                if (count == maxVal(maxValue))
                    count <= 'h0;
                else
                    count <= count + 1'b1;
            end
            else begin
                if (count == 'h0)
                    count <= maxVal(maxValue);
                else
                    count <= count - 1;
            end
        end
    end
endmodule
