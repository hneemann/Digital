package de.neemann.digital.hdl.vhdl;

import de.neemann.digital.core.BitsException;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.wiring.Splitter;
import de.neemann.digital.hdl.model.*;
import de.neemann.digital.hdl.printer.CodePrinter;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Handles all the splitters in a circuit
 */
public class SplitterHandler {
    private final HDLModel model;
    private final CodePrinter out;
    private ArrayList<PendingSplitter> splitters;

    /**
     * Creates a new instance
     *
     * @param model the model
     * @param out   the output
     */
    public SplitterHandler(HDLModel model, CodePrinter out) {
        this.model = model;
        this.out = out;
        splitters = new ArrayList<>();
    }

    /**
     * Registers a splitter.
     * Creates the required signals.
     *
     * @param node the splitter node
     * @throws BitsException BitsException
     * @throws HDLException  HDLException
     */
    public void register(HDLNode node) throws BitsException, HDLException {
        Signal bus = model.createSignal();
        splitters.add(new PendingSplitter(node, bus));
    }

    /**
     * Writes the architecture part of the splitter
     *
     * @throws BitsException BitsException
     * @throws HDLException  HDLException
     * @throws IOException   IOException
     */
    public void write() throws BitsException, HDLException, IOException {
        for (PendingSplitter ps : splitters)
            ps.write();
        splitters.clear();
    }

    private final class PendingSplitter {
        private final HDLNode node;
        private final Signal bus;
        private final Splitter.Ports inputs;
        private final Splitter.Ports outputs;

        private PendingSplitter(HDLNode node, Signal bus) throws BitsException, HDLException {
            this.node = node;
            this.bus = bus;

            inputs = new Splitter.Ports(node.get(Keys.INPUT_SPLIT));
            outputs = new Splitter.Ports(node.get(Keys.OUTPUT_SPLIT));

            bus.setBits(inputs.getBits());
        }

        private void write() throws IOException, HDLException {
            bus.setIsWritten();

            for (int i = 0; i < node.getPorts().getInputs().size(); i++) {
                Splitter.Port p = inputs.getPort(i);
                out.print(bus.getName()).print('(');
                writeRange(p);
                out.print(") <= ");
                out.print(node.getPorts().getInputs().get(i).getSignal().getName());
                out.println(";");
            }

            for (int i = 0; i < node.getPorts().getOutputs().size(); i++) {
                Splitter.Port p = outputs.getPort(i);
                Signal outSig = node.getPorts().getOutputs().get(i).getSignal();
                if (outSig != null) {
                    outSig.setIsWritten();
                    out.print(outSig.getName());
                    out.print(" <= ");
                    out.print(bus.getName()).print('(');
                    writeRange(p);
                    out.println(");");
                }
            }
        }

        private void writeRange(Splitter.Port p) throws IOException {
            if (p.getBits() > 1) {
                out.print(p.getPos() + p.getBits() - 1);
                out.print(" downto ");
                out.print(p.getPos());
            } else
                out.print(p.getPos());
        }
    }
}
