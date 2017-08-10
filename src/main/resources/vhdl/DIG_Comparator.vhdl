LIBRARY ieee;
USE ieee.std_logic_1164.all;

entity DIG_Comparator is
  port (
    PORT_gr: out std_logic;
    PORT_eq: out std_logic;
    PORT_le: out std_logic;
    PORT_a: in {{data}};
    PORT_b: in {{data}} );
end DIG_Comparator;

architecture DIG_Comparator_arch of DIG_Comparator is

begin

  process(PORT_a,PORT_b)
  begin
    if (PORT_a > PORT_b ) then
      PORT_le <= '0';
      PORT_eq <= '0';
      PORT_gr <= '1';
    elsif (PORT_a < PORT_b) then
      PORT_le <= '1';
      PORT_eq <= '0';
      PORT_gr <= '0';
    else
      PORT_le <= '0';
      PORT_eq <= '1';
      PORT_gr <= '0';
    end if;
  end process;

end DIG_Comparator_arch;