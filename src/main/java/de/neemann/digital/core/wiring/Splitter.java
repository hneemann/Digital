package de.neemann.digital.core.wiring;

import de.neemann.digital.core.*;
import de.neemann.digital.core.element.*;
import de.neemann.digital.lang.Lang;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.StringTokenizer;

/**
 * The splitter
 *
 * @author hneemann
 */
public class Splitter implements Element {

    /**
     * Th splitters type description
     */
    public static final ElementTypeDescription DESCRIPTION
            = new SplitterTypeDescription()
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.INPUT_SPLIT)
            .addAttribute(Keys.OUTPUT_SPLIT)
            .setShortName("");

    private final ObservableValue[] outputs;
    private final Ports inPorts;
    private final Ports outPorts;
    private ObservableValue[] inputs;


    private static class SplitterTypeDescription extends ElementTypeDescription {
        SplitterTypeDescription() {
            super(Splitter.class);
        }

        @Override
        public PinDescription[] getInputDescription(ElementAttributes elementAttributes) throws BitsException {
            Ports p = new Ports(elementAttributes.get(Keys.INPUT_SPLIT));
            return p.getNames(PinDescription.Direction.input);
        }

    }

    /**
     * creates a new instance
     *
     * @param attributes the attributes
     * @throws BitsException BitsException
     */
    public Splitter(ElementAttributes attributes) throws BitsException {
        outPorts = new Ports(attributes.get(Keys.OUTPUT_SPLIT));
        outputs = outPorts.getOutputs();
        inPorts = new Ports(attributes.get(Keys.INPUT_SPLIT));
    }

    @Override
    public void setInputs(ObservableValue... inputs) throws NodeException {
        if (inPorts.getBits() != outPorts.getBits())
            throw new BitsException(Lang.get("err_splitterBitsMismatch"), null, combine(inputs, outputs));

        this.inputs = inputs;
        for (int i = 0; i < inputs.length; i++) {
            Port inPort = inPorts.getPort(i);
            if (inPort.getBits() != inputs[i].getBits())
                throw new BitsException(Lang.get("err_splitterBitsMismatch"), null, inputs[i]);
        }

        for (Port out : outPorts)
            fillOutput(out);
    }

    private void fillOutput(Port out) throws NodeException {
        for (Port in : inPorts) {
            if (in.getPos() + in.getBits() <= out.getPos() || out.getPos() + out.getBits() <= in.getPos())
                continue; // this input is not needed to fill the output!!!

            // out is filled completely by the actual single input value!
            if (out.getPos() >= in.getPos()
                    && out.getPos() + out.getBits() <= in.getPos() + in.getBits()) {

                final int bitPos = out.getPos() - in.getPos();
                final ObservableValue inValue = inputs[in.number];
                final ObservableValue outValue = outputs[out.number];
                inValue.addObserverToValue(new Observer() {
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
                inputs[in.number].addObserverToValue(new Observer() {
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
                inputs[in.number].addObserverToValue(new Observer() {
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
            inputs[in.number].addObserverToValue(new Observer() {
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
        // a splitter has no nodes, it works without a delay
    }

    @Override
    public void init(Model m) {
        for (ObservableValue v : inputs)
            v.hasChanged();
    }

    static final class Ports implements Iterable<Port> {
        private final ArrayList<Port> ports;
        private int bits;

        Ports(String definition) throws BitsException {
            StringTokenizer st = new StringTokenizer(definition, ",", false);
            ports = new ArrayList<>();
            bits = 0;
            while (st.hasMoreTokens()) {
                try {
                    String strVal = st.nextToken().trim();
                    int pos = strVal.indexOf('*');
                    if (pos < 0) {
                        Port p = new Port(Integer.decode(strVal), bits, ports.size());
                        ports.add(p);
                        bits += p.getBits();
                    } else {
                        int b = Integer.decode(strVal.substring(0, pos).trim());
                        int count = Integer.decode(strVal.substring(pos + 1).trim());
                        for (int i = 0; i < count; i++) {
                            Port p = new Port(b, bits, ports.size());
                            ports.add(p);
                            bits += p.getBits();
                        }
                    }
                } catch (RuntimeException e) {
                    throw new BitsException(Lang.get("err_spitterDefSyntaxError", definition), null);
                }
            }
            if (ports.isEmpty()) {
                ports.add(new Port(1, 0, 0));
                bits = 1;
            }

        }

        public int getBits() {
            return bits;
        }

        public PinDescription[] getNames(PinDescription.Direction dir) {
            PinInfo[] name = new PinInfo[ports.size()];
            for (int i = 0; i < name.length; i++)
                name[i] = new PinInfo(ports.get(i).getName(), null, dir);

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

        Port(int bits, int pos, int number) {
            this.bits = bits;
            this.pos = pos;
            this.number = number;
            if (bits == 1)
                name = "" + pos;
            else if (bits == 2)
                name = "" + pos + "," + (pos + 1);
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

    /**
     * combines two arrays of {@link ObservableValue}s to a single array.
     *
     * @param inputs  first array
     * @param outputs second array
     * @return the combined array
     */
    public static ObservableValue[] combine(ObservableValue[] inputs, ObservableValue[] outputs) {
        ObservableValue[] com = Arrays.copyOf(inputs, inputs.length + outputs.length);
        System.arraycopy(outputs, 0, com, inputs.length, outputs.length);
        return com;
    }

}
