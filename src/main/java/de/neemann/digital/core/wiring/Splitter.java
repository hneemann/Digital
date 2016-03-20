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
            registerObserversFor(inPort);
        }
    }

    private void registerObserversFor(Port in) throws NodeException {
        Observer observer = outPorts.getSingleTargetObserver(in, inputs, outputs);
        if (observer != null) {
            inputs[in.number].addObserver(observer);
            return;
        }

        for (Port out : outPorts) {
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
            }
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

        /**
         * Checks if there is a single out target port for the input port
         *
         * @param inPort
         * @param inputs
         * @param outputs
         */
        public Observer getSingleTargetObserver(Port inPort, ObservableValue[] inputs, ObservableValue[] outputs) {
            int pos = inPort.getPos();
            int bits = inPort.getBits();

            for (Port outPort : ports) {
                if (outPort.getPos() <= pos && pos + bits <= outPort.getPos() + outPort.getBits()) {
                    final int bitPos = pos - outPort.getPos();
                    final int mask = ~(((1 << inPort.bits) - 1) << bitPos);
                    final ObservableValue inValue = inputs[inPort.number];
                    final ObservableValue outValue = outputs[outPort.number];
                    return new Observer() {
                        @Override
                        public void hasChanged() {
                            long in = inValue.getValue();
                            long out = outValue.getValue();
                            outValue.setValue((out & mask) | (in << bitPos));
                        }
                    };
                }
            }
            return null;
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
