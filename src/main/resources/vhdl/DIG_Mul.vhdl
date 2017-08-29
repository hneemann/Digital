LIBRARY ieee;
USE ieee.std_logic_1164.all;
USE ieee.numeric_std.all;

entity DIG_Mul is
  port (
    PORT_a: in {{data}};
    PORT_b: in {{data}};
    PORT_mul: out std_logic_vector ((bitCount*2-1) downto 0) );
end DIG_Mul;

architecture DIG_Mul_arch of DIG_Mul is
begin
    PORT_mul <= std_logic_vector(unsigned(PORT_a) * unsigned(PORT_b));
end DIG_Mul_arch;