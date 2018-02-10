LIBRARY ieee;
USE ieee.std_logic_1164.all;
USE ieee.std_logic_unsigned.all;

entity DIG_Counter is
  port (
    PORT_out: out {{data}};
    PORT_ovf: out std_logic;
    PORT_C: in std_logic;
    PORT_en: in std_logic;
    PORT_clr: in std_logic );
end DIG_Counter;

architecture DIG_Counter_arch of DIG_Counter is
   signal count : {{data}} := {{zero}};
begin
    process (PORT_C, PORT_clr, PORT_en)
    begin
      if rising_edge(PORT_C) then
        if PORT_clr='1' then
          count <= {{zero}};
        elsif PORT_en='1' then
          count <= count + 1;
        end if;
      end if;
    end process;

    PORT_out <= count;
    PORT_ovf <= PORT_en when count = ((2**bitCount)-1) else '0';
end DIG_Counter_arch;