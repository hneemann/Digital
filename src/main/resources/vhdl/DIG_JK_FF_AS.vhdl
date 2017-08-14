LIBRARY ieee;
USE ieee.std_logic_1164.all;

entity DIG_JK_FF_AS is
  generic (Default : std_logic);
  port (
    PORT_Q: out std_logic;
    PORT_notQ: out std_logic;
    PORT_Set: in std_logic;
    PORT_J: in std_logic;
    PORT_C: in std_logic;
    PORT_K: in std_logic;
    PORT_Clr: in std_logic );
end DIG_JK_FF_AS;

architecture DIG_JK_FF_AS_arch of DIG_JK_FF_AS is
  signal state: std_logic := Default;
begin
    process (PORT_C, PORT_Clr, PORT_Set)
    begin
        if (PORT_Set='1') then
            state <= '1';
        elsif (PORT_Clr='1') then
            state <= '0';
        elsif rising_edge(PORT_C) then
          if (PORT_J='0' and PORT_K='1') then
             state <= '0';
          elsif (PORT_J='1' and PORT_K='0') then
             state <= '1';
          elsif (PORT_J='1' and PORT_K='1') then
             state <= not (state);
          end if;
        end if;
    end process;

    PORT_Q <= state;
    PORT_notQ <= NOT( state );
end DIG_JK_FF_AS_arch;