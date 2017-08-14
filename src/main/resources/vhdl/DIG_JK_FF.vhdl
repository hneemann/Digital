LIBRARY ieee;
USE ieee.std_logic_1164.all;

entity DIG_JK_FF is
  generic (Default : std_logic);
  port (
    PORT_Q: out std_logic;
    PORT_notQ: out std_logic;
    PORT_J: in std_logic;
    PORT_C: in std_logic;
    PORT_K: in std_logic );
end DIG_JK_FF;

architecture DIG_JK_FF_arch of DIG_JK_FF is
  signal temp: std_logic := Default;
begin
  process (PORT_C)
  begin
    if rising_edge(PORT_C) then
      if (PORT_J='0' and PORT_K='1') then
         temp <= '0';
      elsif (PORT_J='1' and PORT_K='0') then
         temp <= '1';
      elsif (PORT_J='1' and PORT_K='1') then
         temp <= not (temp);
      end if;
    end if;
  end process;
  PORT_Q <= temp;
  PORT_notQ <= NOT( temp );
end DIG_JK_FF_arch;