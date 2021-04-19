LIBRARY ieee;
USE ieee.std_logic_1164.all;
USE ieee.std_logic_unsigned.all;

entity add is
  port (
    a: in std_logic_vector(3 downto 0);
    b: in std_logic_vector(3 downto 0);
    c_i: in std_logic;
    s: out std_logic_vector(3 downto 0);
    c_o: out std_logic );
end add;

architecture add_arch of add is
   signal temp : std_logic_vector(4 downto 0);
begin
   temp <= ('0' & a) + b + c_i;

   s    <= temp(3 downto 0);
   c_o  <= temp(4);
end add_arch;
