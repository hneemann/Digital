LIBRARY ieee;
USE ieee.std_logic_1164.all;

entity DIG_D_FF is
   port ( PORT_D  : in {{data}};
          PORT_C  : in std_logic;
          PORT_Q  : out {{data}};
          PORT_notQ : out {{data}} );
end DIG_D_FF;

architecture DIG_D_FF_arch of DIG_D_FF is

   signal state : {{data}};

begin
   PORT_Q    <= state;
   PORT_notQ <= NOT( state );

   ff : process( PORT_D, PORT_C )
   begin
      if (PORT_C'event AND (PORT_C = '1')) then
        state  <= PORT_D;
      end if;
   end process ff;
end DIG_D_FF_arch;