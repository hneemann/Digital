package de.neemann.digital.core.wiring;

import de.neemann.digital.core.*;
import de.neemann.digital.core.element.AttributeKey;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.gui.draw.elements.PinException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

/**
 * @author hneemann
 */
public class Splitter implements Element {

    public static final ElementTypeDescription DESCRIPTION
            = new SplitterTypeDescription()
            .addAttribute(AttributeKey.InputSplit)
            .addAttribute(AttributeKey.OutputSplit)
            .setShortName("");

    private final ObservableValue[] outputs;
    private final Ports inPorts;
    private final Ports outPorts;
    private ObservableValue[] inputs;


    private static class SplitterTypeDescription extends ElementTypeDescription {
        public SplitterTypeDescription() {
            super(Splitter.class);
        }

        @Override
        public String[] getInputNames(ElementAttributes elementAttributes) {
            Ports p = new Ports(elementAttributes.get(AttributeKey.InputSplit));
            return p.getNames();
        }

    }

    public Splitter(ElementAttributes attributes) throws PinException {
        outPorts = new Ports(attributes.get(AttributeKey.OutputSplit));
        outputs = outPorts.getOutputs();
        inPorts = new Ports(attributes.get(AttributeKey.InputSplit));
        if (inPorts.getBits() != outPorts.getBits())
            throw new PinException("splitterPortMismatch");
    }

    @Override
    public void setInputs(ObservableValue... inputs) throws NodeException {
        this.inputs = inputs;
        for (int i = 0; i < inputs.length; i++) {
            Port inPort = inPorts.getPort(i);
            if (inPort.getBits() != inputs[i].getBits())
                throw new BitsException("splitterBitsMismatch", inputs[i]);
        }

        for (Port out : outPorts)
            fillOutput(out);
    }

    private void fillOutput(Port out) throws NodeException {
        for (Port in : inPorts) {
            if (in.getPos() + in.getBits() <= out.getPos() || out.getPos() + out.getBits() <= in.getPos())
                continue; // this input is not needed to fill out!!!

            // out is filled completely by this single input value!
            if (out.getPos() >= in.getPos() &&
                    out.getPos() + out.getBits() <= in.getPos() + in.getBits()) {

                final int bitPos = out.getPos() - in.getPos();
                final ObservableValue inValue = inputs[in.number];
                final ObservableValue outValue = outputs[out.number];
                inValue.addObserver(new Observer() {
                    @Override
                    public void hasChanged() {
                        outValue.setValue(inValue.getValue() >> bitPos);
                    }
                });
                break; // done!! out is completely filled!
            }

            // complete in value needs to be copied to a part of the output
            if (out.getPos() <= in.getPos() && in.getPos() + in.getBits() <= out.getPos() + out.getBits()) {
                final int bitPos = in.getPos() - out.getPos();
                final long mask = ~(((1L << in.bits) - 1) << bitPos);
                final ObservableValue inValue = inputs[in.number];
                final ObservableValue outValue = outputs[out.number];
                inputs[in.number].addObserver(new Observer() {
                    @Override
                    public void hasChanged() {
                        long in = inValue.getValue();
                        long out = outValue.getValue();
                        outValue.setValue((out & mask) | (in << bitPos));
                    }
                });
                continue; // done with this input, its completely copied to the output!
            }

            // If this point is reached, a part of the input needs to be copied to a part of the output!

            // upper part of input needs to be copied to the lower part of the output
            if (in.getPos() < out.getPos()) {
                final int bitsToCopy = in.getPos() + in.getBits() - out.getPos();
                final long mask = ~((1L << bitsToCopy) - 1);
                final int shift = out.getPos() - in.getPos();
                final ObservableValue inValue = inputs[in.number];
                final ObservableValue outValue = outputs[out.number];
                inputs[in.number].addObserver(new Observer() {
                    @Override
                    public void hasChanged() {
                        long in = inValue.getValue();
                        long out = outValue.getValue();
                        outValue.setValue((out & mask) | (in >> shift));
                    }
                });
                continue;
            }

            // lower part of input needs to be copied to the upper part of the output
            final int bitsToCopy = out.getPos() + out.getBits() - in.getPos();
            final int shift = in.getPos() - out.getPos();
            final long mask = ~(((1L << bitsToCopy) - 1) << shift);
            final ObservableValue inValue = inputs[in.number];
            final ObservableValue outValue = outputs[out.number];
            inputs[in.number].addObserver(new Observer() {
                @Override
                public void hasChanged() {
                    long in = inValue.getValue();
                    long out = outValue.getValue();
                    outValue.setValue((out & mask) | (in << shift));
                }
            });

        }
    }

    @Override
    public ObservableValue[] getOutputs() {
        return outputs;
    }

    @Override
    public void registerNodes(Model model) {
    }

    public static final class Ports implements Iterable<Port> {
        private final ArrayList<Port> ports;
        private int bits;

        public Ports(String definition) {
            StringTokenizer st = new StringTokenizer(definition, ",", false);
            ports = new ArrayList<>();
            bits = 0;
            while (st.hasMoreTokens()) {
                Port p = new Port(Integer.decode(st.nextToken().trim()), bits, ports.size());
                ports.add(p);
                bits += p.getBits();
            }
            if (ports.isEmpty()) {
                ports.add(new Port(1, 0, 0));
                bits = 1;
            }

        }

        public int getBits() {
            return bits;
        }

        public String[] getNames() {
            String[] name = new String[ports.size()];
            for (int i = 0; i < name.length; i++)
                name[i] = ports.get(i).getName();

            return name;
        }

        public ObservableValue[] getOutputs() {
            ObservableValue[] outputs = new ObservableValue[ports.size()];
            for (int i = 0; i < ports.size(); i++) {
                Port p = ports.get(i);
                outputs[i] = new ObservableValue(p.getName(), p.getBits());
            }
            return outputs;
        }

        public Port getPort(int i) {
            return ports.get(i);
        }

        @Override
        public Iterator<Port> iterator() {
            return ports.iterator();
        }
    }

    private static final class Port {

        private final String name;
        private final int bits;
        private final int pos;
        private final int number;

        public Port(int bits, int pos, int number) {
            this.bits = bits;
            this.pos = pos;
            this.number = number;
            if (bits == 1)
                name = "" + pos;
            else
                name = "" + pos + "-" + (pos + bits - 1);
        }

        public int getBits() {
            return bits;
        }

        public int getPos() {
            return pos;
        }

        public String getName() {
            return name;
        }
    }
}
