LIBRARY ieee;
USE ieee.std_logic_1164.all;

entity DIG_Reset is
  generic (
    invertOutput : std_logic );
  port (
    PORT_Reset: out std_logic );
end DIG_Reset;

architecture DIG_Reset_arch of DIG_Reset is
begin
    -- ToDo: how to deal with the reset pin?
    PORT_Reset <= invertOutput;
end DIG_Reset_arch;