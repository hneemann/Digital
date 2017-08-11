package de.neemann.digital.hdl.vhdl.boards;

import de.neemann.digital.hdl.model.HDLModel;
import de.neemann.digital.hdl.model.Port;
import de.neemann.digital.hdl.printer.CodePrinter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Creates the needed vivado files
 */
public class Vivado implements BoardInterface {

    private final HDLModel model;
    private final String pinIoType;
    private final String clockPin;
    private final int periodns;

    /**
     * Creates a new instance
     *
     * @param model     the model
     * @param pinIoType the pin output type
     * @param clockPin  the pin the clock is connected to
     * @param periodns  the clock period in nano seconds
     */
    public Vivado(HDLModel model, String pinIoType, String clockPin, int periodns) {
        this.model = model;
        this.pinIoType = pinIoType;
        this.clockPin = clockPin;
        this.periodns = periodns;
    }

    @Override
    public void writeFiles(File path) throws IOException {
        File f = new File(path.getParentFile(), path.getName().replace('.', '_') + "_constrains.xdc");
        try (CodePrinter out = new CodePrinter(new FileOutputStream(f))) {
            for (Port p : model.getPorts()) {
                String pinNumber = p.getPinNumber();
                if (pinNumber == null || pinNumber.length() == 0)
                    throw new IOException("pin without a number: " + p.getName());
                out.print("set_property PACKAGE_PIN ").print(pinNumber).print(" [get_ports ").print(p.getName()).println("]");
                out.print("set_property IOSTANDARD ").print(pinIoType).print(" [get_ports ").print(p.getName()).println("]");

                if (pinNumber.equals(clockPin))
                    out
                            .print("create_clock -add -name sys_clk_pin -period ")
                            .print(periodns)
                            .print(" -waveform {0 5} [get_ports ")
                            .print(p.getName())
                            .println("]");


                out.println();
            }

            out.println("set_property CFGBVS VCCO  [current_design]");
            out.println("set_property CONFIG_VOLTAGE 3.3 [current_design]");
        }
    }
}
