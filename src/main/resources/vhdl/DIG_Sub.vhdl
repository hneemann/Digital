LIBRARY ieee;
USE ieee.std_logic_1164.all;
USE ieee.std_logic_unsigned.all;

entity DIG_Sub is
  port (
    PORT_s: out {{data}};
    PORT_c_o: out std_logic;
    PORT_a: in {{data}};
    PORT_b: in {{data}};
    PORT_c_i: in std_logic );
end DIG_Sub;

architecture DIG_Sub_arch of DIG_Sub is
   signal temp : std_logic_vector(bitCount downto 0);
begin
   temp <= ('0' & PORT_a) - ('0' & PORT_b)  - ('0' & PORT_c_i);

   PORT_s    <= temp((bitCount-1) downto 0);
   PORT_c_o  <= temp(bitCount);
end DIG_Sub_arch;
