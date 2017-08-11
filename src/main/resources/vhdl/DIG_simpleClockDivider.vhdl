LIBRARY ieee;
USE ieee.std_logic_1164.all;
USE ieee.numeric_std.all;
USE ieee.std_logic_unsigned.all;

entity DIG_simpleClockDivider is
  generic ( bits : integer;
            maxCounter : integer );
  port (
    PORT_out: out std_logic;
    PORT_in: in std_logic );
end DIG_simpleClockDivider;

architecture DIG_simpleClockDivider_arch of DIG_simpleClockDivider is
  signal counter: std_logic_vector((bits-1) downto 0) := (others => '0');
  signal state: std_logic;
begin
  process (PORT_in)
  begin
    if rising_edge(PORT_in) then
       if counter = std_logic_vector(to_unsigned(maxCounter, bits)) then
          counter <= (others => '0');
          state <= NOT (state);
       else
          counter <= counter+1;
       end if;
    end if;
  end process;
  PORT_out <= state;
end DIG_simpleClockDivider_arch;