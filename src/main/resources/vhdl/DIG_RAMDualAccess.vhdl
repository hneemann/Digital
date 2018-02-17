LIBRARY ieee;
USE ieee.std_logic_1164.all;
USE ieee.numeric_std.all;

entity DIG_RAMDualAccess is
  generic (
    Bits : integer;
    AddrBits : integer );
  port (
    PORT_1D: out std_logic_vector ((Bits-1) downto 0);
    PORT_2D: out std_logic_vector ((Bits-1) downto 0);
    PORT_str: in std_logic;
    PORT_C: in std_logic;
    PORT_ld: in std_logic;
    PORT_1A: in std_logic_vector ((AddrBits-1) downto 0);
    PORT_1Din: in std_logic_vector ((Bits-1) downto 0);
    PORT_2A: in std_logic_vector ((AddrBits-1) downto 0) );
end DIG_RAMDualAccess;

architecture DIG_RAMDualAccess_arch of DIG_RAMDualAccess is
    -- CAUTION: uses distributed RAM
    type memoryType is array(0 to (2**AddrBits)-1) of STD_LOGIC_VECTOR((Bits-1) downto 0);
    signal memory : memoryType;
begin
  process ( PORT_C )
  begin
    if rising_edge(PORT_C) AND (PORT_str='1') then
      memory(to_integer(unsigned(PORT_1A))) <= PORT_1Din;
    end if;
  end process;
  PORT_1D <= memory(to_integer(unsigned(PORT_1A))) when PORT_ld='1' else (others => 'Z');
  PORT_2D <= memory(to_integer(unsigned(PORT_2A)));
end DIG_RAMDualAccess_arch;
