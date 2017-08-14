LIBRARY ieee;
USE ieee.std_logic_1164.all;

entity DIG_D_FF_AS is
  port (
    PORT_Q: out {{data}};
    PORT_notQ: out {{data}};
    PORT_Set: in std_logic;
    PORT_D: in {{data}};
    PORT_C: in std_logic;
    PORT_Clr: in std_logic );
end DIG_D_FF_AS;

architecture DIG_D_FF_AS_arch of DIG_D_FF_AS is
  signal state : {{data}} := {{zero}};
begin

    process ( PORT_Set, PORT_Clr, PORT_C )
    begin
        if (PORT_Set='1') then
            state <= NOT({{zero}});
        elsif (PORT_Clr='1') then
            state <= {{zero}};
        elsif rising_edge(PORT_C) then
            state <= PORT_D;
        end if;
    end process;

    PORT_Q <= state;
    PORT_notQ <= NOT( state );
end DIG_D_FF_AS_arch;
