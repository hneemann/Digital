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
   signal ovf : std_logic := '0';
begin
    process (PORT_C, PORT_clr)
    begin
      if PORT_clr='1' then
        count <= {{zero}};
      elsif rising_edge(PORT_C) then
        ovf <= '0';
        if PORT_en='1' then
          if count = ((2**bitCount)-1) then
            ovf <= '1';
          end if;
          count <= count + 1;
        end if;
      end if;
    end process;

    PORT_out <= count;
    PORT_ovf <= ovf;
end DIG_Counter_arch;