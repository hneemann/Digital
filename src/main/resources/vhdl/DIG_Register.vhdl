LIBRARY ieee;
USE ieee.std_logic_1164.all;

entity DIG_Register is
  port (
    PORT_Q: out {{data}};
    PORT_D: in {{data}};
    PORT_C: in std_logic;
    PORT_en: in std_logic );
end DIG_Register;

architecture DIG_Register_arch of DIG_Register is
  signal state : {{data}} := {{zero}};
begin
   PORT_Q <= state;

   process ( PORT_C )
   begin
      if rising_edge(PORT_C) and (PORT_en='1') then
        state <= PORT_D;
      end if;
   end process;
end DIG_Register_arch;