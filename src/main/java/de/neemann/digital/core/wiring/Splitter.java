/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.wiring;

import de.neemann.digital.core.*;
import de.neemann.digital.core.element.*;
import de.neemann.digital.lang.Lang;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

/**
 * The Splitter.
 */
public class Splitter implements Element {

    /**
     * Create a one to N splitter
     *
     * @param bits number of outputs
     * @return the splitter
     */
    public static Splitter createOneToN(int bits) {
        Ports in = new Ports();
        in.add(new Port(0, bits));
        Ports out = new Ports();
        for (int i = 0; i < bits; i++)
            out.add(new Port(i, 1));
        return new Splitter(in, out);
    }

    /**
     * Create a N to one splitter
     *
     * @param bits number of inputs
     * @return the splitter
     */
    public static Splitter createNToOne(int bits) {
        Ports in = new Ports();
        for (int i = 0; i < bits; i++)
            in.add(new Port(i, 1));
        Ports out = new Ports();
        out.add(new Port(0, bits));
        return new Splitter(in, out);
    }

    /**
     * The splitters type description
     */
    public static final ElementTypeDescription DESCRIPTION
            = new SplitterTypeDescription()
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.MIRROR)
            .addAttribute(Keys.INPUT_SPLIT)
            .addAttribute(Keys.OUTPUT_SPLIT)
            .addAttribute(Keys.SPLITTER_SPREADING)
            .setShortName("")
            .supportsHDL();

    private final ObservableValues outputs;
    private final Ports inPorts;
    private final Ports outPorts;
    private ObservableValues inputs;


    private static class SplitterTypeDescription extends ElementTypeDescription {
        SplitterTypeDescription() {
            super(Splitter.class);
        }

        @Override
        public PinDescriptions getInputDescription(ElementAttributes elementAttributes) throws BitsException {
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
        this(new Ports(attributes.get(Keys.INPUT_SPLIT)),
                new Ports(attributes.get(Keys.OUTPUT_SPLIT)));
    }

    private Splitter(Ports inPorts, Ports outPorts) {
        this.inPorts = inPorts;
        this.outPorts = outPorts;
        outputs = outPorts.getOutputs();
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        this.inputs = inputs;

        inPorts.checkInputConsistency();

        if (inPorts.getBits() < outPorts.getBits())
            throw new BitsException(Lang.get("err_splitterBitsMismatch"), ImmutableList.combine(inputs, outputs));

        for (int i = 0; i < inputs.size(); i++) {
            Port inPort = inPorts.getPort(i);
            if (inPort.getBits() != inputs.get(i).getBits())
                throw new BitsException(Lang.get("err_splitterBitsMismatch"), inputs);
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
                final ObservableValue inValue = inputs.get(in.number);
                final ObservableValue outValue = outputs.get(out.number);
                inValue.addObserverToValue(new NodeWithoutDelay(outValue) {
                    @Override
                    public void hasChanged() {
                        outValue.set(inValue.getValue() >>> bitPos, inValue.getHighZ() >>> bitPos);
                    }
                });
                break; // done!! out is completely filled!
            }

            // complete in value needs to be copied to a part of the output
            if (out.getPos() <= in.getPos() && in.getPos() + in.getBits() <= out.getPos() + out.getBits()) {
                final int bitPos = in.getPos() - out.getPos();
                final long mask = ~Bits.up(Bits.mask(in.bits), bitPos);
                final ObservableValue inValue = inputs.get(in.number);
                final ObservableValue outValue = outputs.get(out.number);
                inputs.get(in.number).addObserverToValue(new NodeWithoutDelay(outValue) {
                    @Override
                    public void hasChanged() {
                        long in1 = inValue.getValue();
                        long out1 = outValue.getValue();
                        long inz1 = inValue.getHighZ();
                        long outz1 = outValue.getHighZ();
                        outValue.set((out1 & mask) | (in1 << bitPos), (outz1 & mask) | (inz1 << bitPos));
                    }
                });
                continue; // done with this input, its completely copied to the output!
            }

            // If this point is reached, a part of the input needs to be copied to a part of the output!

            // upper part of input needs to be copied to the lower part of the output
            if (in.getPos() < out.getPos()) {
                final int bitsToCopy = in.getPos() + in.getBits() - out.getPos();
                final long mask = ~Bits.mask(bitsToCopy);
                final int shift = out.getPos() - in.getPos();
                final ObservableValue inValue = inputs.get(in.number);
                final ObservableValue outValue = outputs.get(out.number);
                inputs.get(in.number).addObserverToValue(new NodeWithoutDelay(outValue) {
                    @Override
                    public void hasChanged() {
                        long in12 = inValue.getValue();
                        long out12 = outValue.getValue();
                        long inz12 = inValue.getHighZ();
                        long outz12 = outValue.getHighZ();
                        outValue.set((out12 & mask) | (in12 >>> shift), (outz12 & mask) | (inz12 >>> shift));
                    }
                });
                continue;
            }

            // lower part of input needs to be copied to the upper part of the output
            final int bitsToCopy = out.getPos() + out.getBits() - in.getPos();
            final int shift = in.getPos() - out.getPos();
            final long mask = ~Bits.up(Bits.mask(bitsToCopy), shift);
            final ObservableValue inValue = inputs.get(in.number);
            final ObservableValue outValue = outputs.get(out.number);
            inputs.get(in.number).addObserverToValue(new NodeWithoutDelay(outValue) {
                @Override
                public void hasChanged() {
                    long in13 = inValue.getValue();
                    long out13 = outValue.getValue();
                    long inz13 = inValue.getHighZ();
                    long outz13 = outValue.getHighZ();
                    outValue.set((out13 & mask) | (in13 << shift), (outz13 & mask) | (inz13 << shift));
                }
            });

        }
    }

    @Override
    public ObservableValues getOutputs() {
        return outputs;
    }

    @Override
    public void registerNodes(Model model) {
        // a splitter has no nodes, it works without a delay
    }

    @Override
    public void init(Model m) {
        for (ObservableValue v : inputs)
            v.fireHasChanged();
    }

    /**
     * A splitter port list
     */
    public static final class Ports implements Iterable<Port> {
        private final ArrayList<Port> ports;
        private int bits = 0;

        Ports() {
            ports = new ArrayList<>();
        }

        /**
         * Creates a new instance
         *
         * @param definition the splitter definition
         * @throws BitsException BitsException
         */
        public Ports(String definition) throws BitsException {
            this();
            StringTokenizer st = new StringTokenizer(definition, ",", false);
            while (st.hasMoreTokens()) {
                try {
                    String strVal = st.nextToken().trim();
                    int pos = strVal.indexOf('*');
                    if (pos >= 0) {
                        int b = Integer.decode(strVal.substring(0, pos).trim());
                        int count = Integer.decode(strVal.substring(pos + 1).trim());
                        for (int i = 0; i < count; i++)
                            add(new Port(bits, b));
                    } else {
                        pos = strVal.indexOf('-');
                        if (pos >= 0) {
                            int from = Integer.decode(strVal.substring(0, pos).trim());
                            int to = Integer.decode(strVal.substring(pos + 1).trim());
                            if (to < from) {
                                int z = to;
                                to = from;
                                from = z;
                            }
                            add(new Port(from, to - from + 1));
                        } else
                            add(new Port(bits, Integer.decode(strVal)));
                    }
                } catch (RuntimeException e) {
                    throw new BitsException(Lang.get("err_spitterDefSyntaxError", definition));
                }
            }
            if (ports.isEmpty())
                add(new Port(bits, 1));

            if (bits > 64)
                throw new BitsException(Lang.get("err_spitterToManyBits", definition));
        }

        void checkInputConsistency() throws BitsException {
            long fullMask = Bits.mask(bits);
            for (Port p : ports) {
                long mask = Bits.up(Bits.mask(p.bits), p.pos);

                if ((fullMask & mask) != mask)
                    throw new BitsException(Lang.get("err_splitterNotUnambiguously"));

                fullMask = fullMask & (~mask);
            }
            if (fullMask != 0)
                throw new BitsException(Lang.get("err_splitterNotAllBitsDefined"));
        }

        private void add(Port port) {
            port.setNumber(ports.size());
            ports.add(port);
            int b = port.pos + port.bits;
            if (b > bits)
                bits = b;
        }

        /**
         * @return the number of bits
         */
        public int getBits() {
            return bits;
        }

        private PinDescriptions getNames(PinDescription.Direction dir) {
            PinInfo[] name = new PinInfo[ports.size()];
            for (int i = 0; i < name.length; i++) {
                final Port port = ports.get(i);
                if (port.getBits() == 1)
                    name[i] = new PinInfo(port.getName(), Lang.get("elem_Splitter_pin_in_one", port.getName()), dir);
                else
                    name[i] = new PinInfo(port.getName(), Lang.get("elem_Splitter_pin_in", port.getName()), dir);
            }

            return new PinDescriptions(name);
        }

        ObservableValues getOutputs() {
            ArrayList<ObservableValue> outputs = new ArrayList<>(ports.size());
            for (Port p : ports) {
                if (p.getBits() == 1)
                    outputs.add(new ObservableValue(p.getName(), p.getBits()).setDescription(Lang.get("elem_Splitter_pin_out_one", p.getName())));
                else
                    outputs.add(new ObservableValue(p.getName(), p.getBits()).setDescription(Lang.get("elem_Splitter_pin_out", p.getName())));
            }
            return new ObservableValues(outputs);
        }

        /**
         * returns the port with the given index
         *
         * @param i the ports index
         * @return the port
         */
        public Port getPort(int i) {
            return ports.get(i);
        }

        @Override
        public Iterator<Port> iterator() {
            return ports.iterator();
        }
    }

    /**
     * A single splitter port.
     */
    public static final class Port {
        private final int bits;
        private final int pos;
        private final String name;
        private int number;

        private Port(int pos, int bits) {
            this.pos = pos;
            this.bits = bits;
            if (bits == 1)
                name = "" + pos;
            else if (bits == 2)
                name = "" + pos + "," + (pos + 1);
            else
                name = "" + pos + "-" + (pos + bits - 1);
        }

        /**
         * @return the number of bits in this port
         */
        public int getBits() {
            return bits;
        }

        /**
         * @return the position of this port
         */
        public int getPos() {
            return pos;
        }

        /**
         * @return the name of this port
         */
        public String getName() {
            return name;
        }

        private void setNumber(int number) {
            this.number = number;
        }
    }

}
