package de.neemann.digital.hdl.vhdl.boards;

import de.neemann.digital.analyse.SplitPinString;
import de.neemann.digital.hdl.model.ClockIntegrator;
import de.neemann.digital.hdl.model.HDLModel;
import de.neemann.digital.hdl.model.Port;
import de.neemann.digital.hdl.printer.CodePrinter;
import de.neemann.digital.lang.Lang;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Creates the needed vivado files.
 * Up to now only the constraints file containing the pin assignments is created
 */
public class Vivado implements BoardInterface {

    private final String pinIoType;
    private final String clockPin;
    private final int periodns;
    private final ClockIntegrator clockIntegrator;

    /**
     * Creates a new instance
     *
     * @param pinIoType       the pin output type
     * @param clockPin        the pin the clock is connected to
     * @param periodns        the clock period in nano seconds
     * @param clockIntegrator the clock integrator to use
     */
    public Vivado(String pinIoType, String clockPin, int periodns, ClockIntegrator clockIntegrator) {
        this.pinIoType = pinIoType;
        this.clockPin = clockPin;
        this.periodns = periodns;
        this.clockIntegrator = clockIntegrator;
    }

    @Override
    public void writeFiles(File path, HDLModel model) throws IOException {
        File f = new File(path.getParentFile(), path.getName().replace('.', '_') + "_constraints.xdc");
        try (CodePrinter out = new CodePrinter(new FileOutputStream(f))) {
            writeConstraints(out, model);
        }
    }

    void writeConstraints(CodePrinter out, HDLModel model) throws IOException {
        for (Port p : model.getPorts()) {
            if (p.getBits() == 1) {
                writePin(out, p.getName(), p.getPinNumber());
                if (p.getPinNumber().equals(clockPin))
                    out
                            .print("create_clock -add -name sys_clk_pin -period ")
                            .print(periodns)
                            .print(" -waveform {0 5} [get_ports ")
                            .print(p.getName())
                            .println("]");
            } else {
                SplitPinString pins = SplitPinString.create(p.getPinNumber());
                for (int i = 0; i < p.getBits(); i++)
                    writePin(out, p.getName() + "[" + i + "]", pins.getPin(i));
            }


            out.println();
        }

        out.println("set_property CFGBVS VCCO  [current_design]");
        out.println("set_property CONFIG_VOLTAGE 3.3 [current_design]");
    }

    private void writePin(CodePrinter out, String name, String pinNumber) throws IOException {
        if (pinNumber == null || pinNumber.length() == 0)
            throw new IOException(Lang.get("err_vhdlPin_N_hasNoNumber", name));

        out.print("set_property PACKAGE_PIN ").print(pinNumber).print(" [get_ports ").print(name).println("]");
        out.print("set_property IOSTANDARD ").print(pinIoType).print(" [get_ports ").print(name).println("]");
    }

    @Override
    public ClockIntegrator getClockIntegrator() {
        return clockIntegrator;
    }

}
