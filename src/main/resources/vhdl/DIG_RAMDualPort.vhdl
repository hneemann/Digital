LIBRARY ieee;
USE ieee.std_logic_1164.all;
USE ieee.numeric_std.all;

entity DIG_RAMDualPort is
  generic (
    Bits : integer;
    AddrBits : integer );
  port (
    PORT_D: out std_logic_vector ((Bits-1) downto 0);
    PORT_A: in std_logic_vector ((AddrBits-1) downto 0);
    PORT_Din: in std_logic_vector ((Bits-1) downto 0);
    PORT_str: in std_logic;
    PORT_C: in std_logic;
    PORT_ld: in std_logic );
end DIG_RAMDualPort;

architecture DIG_RAMDualPort_arch of DIG_RAMDualPort is
    -- CAUTION: uses distributed RAM
    type memoryType is array(0 to (2**AddrBits)-1) of STD_LOGIC_VECTOR((Bits-1) downto 0);
    signal memory : memoryType;
begin
  process ( PORT_C )
  begin
    if rising_edge(PORT_C) AND (PORT_str='1') then
      memory(to_integer(unsigned(PORT_A))) <= PORT_Din;
    end if;
  end process;
  PORT_D <= memory(to_integer(unsigned(PORT_A))) when PORT_ld='1' else (others => 'Z');
end DIG_RAMDualPort_arch;
