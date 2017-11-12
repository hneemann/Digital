LIBRARY ieee;
USE ieee.std_logic_1164.all;

entity DIG_BitExtender is
  generic ( inputBits  : integer;
            outputBits : integer);
  port (
    PORT_in: in std_logic_vector ((inputBits-1) downto 0);
    PORT_out: out std_logic_vector ((outputBits-1) downto 0) );
end DIG_BitExtender;

architecture DIG_BitExtender_arch of DIG_BitExtender is
begin
    PORT_out((inputBits-2) downto 0) <= PORT_in((inputBits-2) downto 0);
    PORT_out((outputBits-1) downto (inputBits-1)) <= (others => PORT_in(inputBits-1));
end DIG_BitExtender_arch;
