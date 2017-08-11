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

    /**
     * Creates a new instance
     *
     * @param model   the model
     * @param pinIoType the pin output type
     */
    public Vivado(HDLModel model, String pinIoType) {
        this.model = model;
        this.pinIoType = pinIoType;
    }

    @Override
    public void writeFiles(File path) throws IOException {
        File f = new File(path.getParentFile(), path.getName().replace('.', '_') + ".xdr");
        try (CodePrinter out = new CodePrinter(new FileOutputStream(f))) {
            for (Port p : model.getPorts()) {
                out.print("set_property PACKAGE_PIN ").print(p.getPinNumber()).print(" [get_ports ").print(p.getName()).print(" as ").println("]");
                out.print("set_property IOSTANDARD ").print(pinIoType).print(" [get_ports ").print(p.getName()).print(" as ").println("]");
                out.println();
            }
        }
    }
}
