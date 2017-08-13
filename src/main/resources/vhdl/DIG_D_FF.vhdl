LIBRARY ieee;
USE ieee.std_logic_1164.all;

entity DIG_D_FF is
   port ( PORT_D  : in {{data}};
          PORT_C  : in std_logic;
          PORT_Q  : out {{data}};
          PORT_notQ : out {{data}} );
end DIG_D_FF;

architecture DIG_D_FF_arch of DIG_D_FF is

   signal state : {{data}} := {{zero}};

begin
   PORT_Q    <= state;
   PORT_notQ <= NOT( state );

   process(PORT_C)
   begin
      if rising_edge(PORT_C) then
        state  <= PORT_D;
      end if;
   end process;
end DIG_D_FF_arch;