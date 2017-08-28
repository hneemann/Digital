LIBRARY ieee;
USE ieee.std_logic_1164.all;

Library UNISIM;
use UNISIM.vcomponents.all;

entity DIG_MMCME2_BASE_CC is
  generic (
    D_PARAM : integer;
    M_PARAM : real;
    DIV_PARAM : integer;
    DIV4_PARAM : integer;
    PERIOD_PARAM: real);
  port (
    PORT_in: in std_logic;
    PORT_out: out std_logic );
end DIG_MMCME2_BASE_CC;

architecture DIG_MMCME2_BASE_CC_arch of DIG_MMCME2_BASE_CC is

    signal DEV_NULL: std_logic;
    signal feedback: std_logic;

begin

DEV_NULL <= '0';

-- code taken from the "Vivado Design Suite 7 Series FPGA Libraries Guide" (UG953)

MMCME2_BASE_inst : MMCME2_BASE
generic map (
BANDWIDTH => "OPTIMIZED", -- Jitter programming (OPTIMIZED, HIGH, LOW)
CLKFBOUT_MULT_F => M_PARAM,
-- Multiply value for all CLKOUT (2.000-64.000).
DIVCLK_DIVIDE => D_PARAM,
-- Master division value (1-106)
CLKFBOUT_PHASE => 0.0,
-- Phase offset in degrees of CLKFB (-360.000-360.000).
CLKIN1_PERIOD => PERIOD_PARAM,
-- Input clock period in ns to ps resolution (i.e. 33.333 is 30 MHz).
-- CLKOUT0_DIVIDE - CLKOUT6_DIVIDE: Divide amount for each CLKOUT (1-128)
CLKOUT1_DIVIDE => 1,
CLKOUT2_DIVIDE => 1,
CLKOUT3_DIVIDE => 1,
CLKOUT4_DIVIDE => DIV4_PARAM,
CLKOUT5_DIVIDE => 1,
CLKOUT6_DIVIDE => DIV_PARAM,
CLKOUT0_DIVIDE_F => 1.0,
-- Divide amount for CLKOUT0 (1.000-128.000).
-- CLKOUT0_DUTY_CYCLE - CLKOUT6_DUTY_CYCLE: Duty cycle for each CLKOUT (0.01-0.99).
CLKOUT0_DUTY_CYCLE => 0.5,
CLKOUT1_DUTY_CYCLE => 0.5,
CLKOUT2_DUTY_CYCLE => 0.5,
CLKOUT3_DUTY_CYCLE => 0.5,
CLKOUT4_DUTY_CYCLE => 0.5,
CLKOUT5_DUTY_CYCLE => 0.5,
CLKOUT6_DUTY_CYCLE => 0.5,
-- CLKOUT0_PHASE - CLKOUT6_PHASE: Phase offset for each CLKOUT (-360.000-360.000).
CLKOUT0_PHASE => 0.0,
CLKOUT1_PHASE => 0.0,
CLKOUT2_PHASE => 0.0,
CLKOUT3_PHASE => 0.0,
CLKOUT4_PHASE => 0.0,
CLKOUT5_PHASE => 0.0,
CLKOUT6_PHASE => 0.0,
CLKOUT4_CASCADE => true, -- Cascade CLKOUT4 counter with CLKOUT6 (FALSE, TRUE)
REF_JITTER1 => 0.0,
-- Reference input jitter in UI (0.000-0.999).
STARTUP_WAIT => TRUE
-- Delays DONE until MMCM is locked (FALSE, TRUE)
)
port map (
-- Clock Outputs: 1-bit (each) output: User configurable clock outputs
CLKOUT4 => PORT_out,
-- 1-bit output: CLKOUT6
-- Feedback Clocks: 1-bit (each) output: Clock feedback ports
CLKFBOUT => feedback,
-- 1-bit output: Feedback clock
--CLKFBOUTB => CLKFBOUTB, -- 1-bit output: Inverted CLKFBOUT
-- Status Ports: 1-bit (each) output: MMCM status ports
--LOCKED => LOCKED,
-- 1-bit output: LOCK
-- Clock Inputs: 1-bit (each) input: Clock input
CLKIN1 => PORT_in,
-- 1-bit input: Clock
-- Control Ports: 1-bit (each) input: MMCM control ports
PWRDWN => DEV_NULL,
-- 1-bit input: Power-down
RST => DEV_NULL,
-- 1-bit input: Reset
-- Feedback Clocks: 1-bit (each) input: Clock feedback ports
CLKFBIN => feedback
-- 1-bit input: Feedback clock
);

end DIG_MMCME2_BASE_CC_arch;
