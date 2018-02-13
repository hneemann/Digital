LIBRARY ieee;
USE ieee.std_logic_1164.all;
USE ieee.numeric_std.all;

entity DIG_RegisterFile is
  generic (
    Bits : integer;
    AddrBits : integer );
  port (
    PORT_Da: out std_logic_vector ((Bits-1) downto 0);
    PORT_Db: out std_logic_vector ((Bits-1) downto 0);
    PORT_Din: in std_logic_vector ((Bits-1) downto 0);
    PORT_we: in std_logic;
    PORT_Rw: in std_logic_vector ((AddrBits-1) downto 0);
    PORT_C: in std_logic;
    PORT_Ra: in std_logic_vector ((AddrBits-1) downto 0);
    PORT_Rb: in std_logic_vector ((AddrBits-1) downto 0) );
end DIG_RegisterFile;

architecture DIG_RegisterFile_arch of DIG_RegisterFile is
    type memoryType is array(0 to (2**AddrBits)-1) of STD_LOGIC_VECTOR((Bits-1) downto 0);
    signal memory : memoryType;
begin
  process ( PORT_C )
  begin
    if rising_edge(PORT_C) AND (PORT_we='1') then
      memory(to_integer(unsigned(PORT_Rw))) <= PORT_Din;
    end if;
  end process;
  PORT_Da <= memory(to_integer(unsigned(PORT_Ra)));
  PORT_Db <= memory(to_integer(unsigned(PORT_Rb)));
end DIG_RegisterFile_arch;
