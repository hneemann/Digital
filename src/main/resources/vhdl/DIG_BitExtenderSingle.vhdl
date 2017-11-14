LIBRARY ieee;
USE ieee.std_logic_1164.all;

entity DIG_BitExtenderSingle is
  generic ( outputBits : integer);
  port (
    PORT_in: in std_logic;
    PORT_out: out std_logic_vector ((outputBits-1) downto 0) );
end DIG_BitExtenderSingle;

architecture DIG_BitExtenderSingle_arch of DIG_BitExtenderSingle is
begin
    PORT_out((outputBits-1) downto 0) <= (others => PORT_in);
end DIG_BitExtenderSingle_arch;
