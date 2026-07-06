/*
 * Copyright (c) 2026 Jonas Meeuws
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.simavr;

import be.jnms.simavr.LibSimAVR;
import be.jnms.simavr.swig.avr_ioport_state_t;
import be.jnms.simavr.swig.avr_irq_t;
import be.jnms.simavr.swig.avr_t;
import be.jnms.simavr.swig.elf_firmware_t;
import be.jnms.simavr.swig.java_method_t;
import be.jnms.simavr.swig.simavr;
import de.neemann.digital.core.*;
import de.neemann.digital.core.element.*;
import de.neemann.digital.draw.elements.PinException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * The Simavr component
 */
public class Simavr extends Node implements Element {
    private static final Logger LOGGER = LoggerFactory.getLogger(Simavr.class);

    public enum Mcu {
        at90usb162    ("AT90usb162",              0, 8, 8, 8),
        atmega48      ("ATmega48/48p/48pa",       0, 8, 7, 8),
        atmega8       ("ATmega8/8l",              0, 8, 7, 8),
        atmega88      ("ATmega88/88p/88pa",       0, 8, 7, 8),
        atmega16      ("ATmega16",                0, 8, 7, 8),
        atmega164     ("ATmega164/164p/164pa",    8, 8, 8, 8),
        atmega168     ("ATmega168/168p/168pa",    0, 8, 7, 8),
        atmega16m1    ("ATmega16m1",              0, 8, 8, 8, 3),
        atmega32      ("ATmega32",                0, 8, 7, 8),
        atmega324     ("ATmega324/324p",          8, 8, 8, 8),
        atmega324a    ("ATmega324a/324pa",        8, 8, 8, 8),
        atmega328     ("ATmega328/328p",          0, 8, 7, 8),
        atmega328pb   ("ATmega328pb",             0, 8, 7, 8, 4),
        atmega32u4    ("ATmega32u4",              8, 8, 8, 8, 8),
        atmega644     ("ATmega644/644p",          0, 8, 8, 8, 8),
        atmega64m1    ("ATmega64m1",              0, 8, 8, 8, 3),
        atmega128     ("ATmega128/128L",          8, 8, 8, 8, 8, 8, 5),
        atmega1280    ("ATmega1280",              0),
        atmega1281    ("ATmega1281",              0),
        atmega1284p   ("ATmega1284p/1284",        0),
        atmega128rfa1 ("ATmega128rfa1",           0),
        atmega128rfr2 ("ATmega128rfr2",           0),
        atmega169p    ("ATmega169p",              8, 8, 8, 8, 8, 8, 6),
        atmega2560    ("ATmega2560",              0),
        attiny13      ("ATtiny13/attiny13a",      0),
        attiny2313    ("ATtiny2313/attiny2313v",  0),
        attiny2313a   ("ATtiny2313a",             0),
        attiny24      ("ATtiny24",                0),
        attiny25      ("ATtiny25",                0),
        attiny4313    ("ATtiny4313",              0),
        attiny44      ("ATtiny44",                0),
        attiny45      ("ATtiny45",                0),
        attiny84      ("ATtiny84",                0),
        attiny85      ("ATtiny85",                0);

        public static class Port {
            public final char name;
            public final int bits;

            public Port(char name, int bits) {
                this.name = name;
                this.bits = bits;
            }
        }

        public final String description;
        public final List<Port> ports;

        Mcu(String description, int... portsBits) {
            this.description = description;
            this.ports = IntStream.range(0, portsBits.length)
                    .mapToObj(i -> {
                        final char name = (char) ('A' + i);
                        final int bits = portsBits[i];
                        if (bits == 0)
                            return null;
                        return new Port(name, bits);
                    })
                    .filter(el -> el != null)
                    .collect(Collectors.toUnmodifiableList());
        }

        @Override
        public String toString() {
            return description;
        }
    }

    public static final ElementTypeDescription DESCRIPTION = new Description();

    static class Description extends ElementTypeDescription {
        public Description() {
            super(Simavr.class);
            addAttribute(Keys.SIMAVR_MCU);
            addAttribute(Keys.SIMAVR_FIRMWARE_FILE_PATH);
            addAttribute(Keys.WIDTH);
        }

        @Override
        public PinDescriptions getInputDescription(ElementAttributes attributes) throws NodeException {
            final List<PinDescription> inputs = new ArrayList<>();
            inputs.add(PinInfo.input("R", "Reset"));
            inputs.add(PinInfo.input("C", "Clock").setClock());
            return new PinDescriptions(inputs.toArray(PinDescription[]::new));
        }

        @Override
        public PinDescriptions getOutputDescriptions(ElementAttributes attributes) throws PinException {
            final Mcu mcu = attributes.get(Keys.SIMAVR_MCU);
            final List<PinDescription> outputs = new ArrayList<>();
            outputs.add(new PinInfo("TxC", "TX Clock", PinDescription.Direction.output, null).setClock());
            outputs.add(new PinInfo("TxD", "TX Data (8-bit)", PinDescription.Direction.output, null));
            for (final Mcu.Port port : mcu.ports) {
                outputs.add(new PinInfo("P%c".formatted(port.name), "Port%c".formatted(port.name), PinDescription.Direction.both, null));
            }
            return new PinDescriptions(outputs.toArray(PinDescription[]::new));
        }
    }

    static class Value {
        public final int bits;
        public final long bitMask;
        public long value;
        public long highZMask;

        Value(ObservableValue observable) {
            bits = observable.getBits();
            bitMask = Bits.mask(bits);
            value = 0;
            highZMask = bitMask;
        }

        void updateFrom(ObservableValue observable) {
            value = observable.getValue();
            highZMask = observable.getHighZ();
        }

        void applyTo(ObservableValue observable) {
            observable.set(value, highZMask);
        }

        boolean getBool() {
            return value != 0;
        }
    }

    public static void onGlobalLogMessage(int level, String message) {
        message = "simavr: " + message.strip();
        if (level == simavr.LOG_OUTPUT) {
            LOGGER.info(message);
        } else if (level == simavr.LOG_ERROR) {
            LOGGER.error(message);
        } else if (level == simavr.LOG_WARNING) {
            LOGGER.warn(message);
        } else if (level == simavr.LOG_TRACE) {
            LOGGER.info(message);
        } else if (level == simavr.LOG_DEBUG) {
            LOGGER.info(message);
        }
    }

    final Mcu mcu;
    final String firmwareFilePath;

    final int outputCount;
    final ObservableValues outputs;
    final Value[] outputValues;

    final int inputCount;
    ObservableValues inputs;
    Value[] inputValues;

    java_method_t onGlobalLogMessageMethod;
    java_method_t onTxCharMethod;
    avr_t avr;
    int cpuState;
    Queue<Long> txQueue;

    /**
     * Creates a new instance
     *
     * @param attributes the attributes
     */
    public Simavr(ElementAttributes attributes) {
        super(true);

        this.mcu = attributes.get(Keys.SIMAVR_MCU);
        this.firmwareFilePath = attributes.get(Keys.SIMAVR_FIRMWARE_FILE_PATH).getPath();

        outputCount = 2 + mcu.ports.size();
        outputs = new ObservableValues(
                Stream.concat(
                        Stream.of(
                                new ObservableValue("TxC", 1),
                                new ObservableValue("TxD", 8)),
                        mcu.ports
                                .stream()
                                .map(port -> new ObservableValue("P%c".formatted(port.name), port.bits)))
                        .toArray(ObservableValue[]::new));
        outputValues = outputs.stream().map(Value::new).toArray(Value[]::new);

        inputCount = 2 + mcu.ports.size();
        inputs = null;
        inputValues = null;

        avr = null;
        cpuState = 0;
        txQueue = null;
    }

    @Override
    public void setInputs(ObservableValues newInputs) throws BitsException {
        inputs = newInputs;

        inputs.get(0).checkBits(1, this, 0); // Reset
        inputs.get(1).checkBits(1, this, 1); // Clock

        // Ports (bidirectional)
        for (int i = 0; i < mcu.ports.size(); i++) {
            final Mcu.Port port = mcu.ports.get(i);
            inputs.get(i + 2).checkBits(port.bits, this, i + 2);
        }

        for (final ObservableValue input : inputs) {
            input.addObserverToValue(this);
        }
        inputValues = inputs.stream().map(Value::new).toArray(Value[]::new);
    }

    @Override
    public ObservableValues getOutputs() {
        return outputs;
    }

    @Override
    public void readInputs() throws NodeException {
        setupIfNeeded();

        final boolean lastResetValue = inputValues[0].getBool();
        final boolean lastClockValue = inputValues[1].getBool();

        for (int i = 0; i < inputCount; i++) {
            inputValues[i].updateFrom(inputs.get(i));
        }

        // Detect Reset rising edge
        if (inputValues[0].getBool() && !lastResetValue) {
            reset();
        }

        // Detect Clock rising edge
        if (inputValues[1].getBool() && !lastClockValue) {
            runOneCycle();
        }
    }

    @Override
    public void writeOutputs() throws NodeException {
        for (int i = 0; i < outputCount; i++) {
            outputValues[i].applyTo(outputs.get(i));
        }
    }

    void setupIfNeeded() {
        if (avr == null) {
            setup();
        }
    }

    void setup() {
        System.err.println("setup()");
        try {
            LibSimAVR.load();
        } catch (IOException e) {
            // TODO
            throw new IllegalStateException(e);
        }

        try {
            this.onGlobalLogMessageMethod = new java_method_t(getClass().getMethod("onGlobalLogMessage", int.class, String.class));
            this.onTxCharMethod = new java_method_t(this, getClass().getMethod("onTxChar", long.class, long.class));
        } catch (NoSuchMethodException e) {
            // TODO
            throw new IllegalStateException(e);
        }

        simavr.avr_global_logger_external_set_java_method(onGlobalLogMessageMethod);

        elf_firmware_t elf = new elf_firmware_t();
        if (simavr.elf_read_firmware(firmwareFilePath, elf) != 0) {
            // TODO
            throw new IllegalArgumentException("failed to read " + firmwareFilePath);
        }

        avr = simavr.avr_make_mcu_by_name(mcu.name());
        cpuState = simavr.cpu_Limbo;
        txQueue = new ArrayDeque<>();

        simavr.avr_init(avr);
        simavr.avr_load_firmware(avr, elf);

        simavr.avr_irq_register_notify(
                simavr.avr_io_getirq(avr, simavr.AVR_IOCTL_UART_GETIRQ((short) '0'), simavr.UART_IRQ_OUTPUT),
                onTxCharMethod);
    }

    public void onTxChar(long irqPtr, long value) {
        avr_irq_t irq = new avr_irq_t(irqPtr, false);
        txQueue.add(value);
    }

    void reset() {
        System.err.println("reset()");
        setup();
    }

    void runOneCycle() {
        setupIfNeeded();

        if (inputValues[0].getBool())
        {
            reset();
            return;
        }

        for (int i = 0; i < mcu.ports.size(); i++) {
            final Mcu.Port port = mcu.ports.get(i);
            final Value value = inputValues[2 + i];
            final long ctl = simavr.AVR_IOCTL_IOPORT_GETIRQ((short) port.name);
            for (int bit = 0; bit < port.bits; bit++) {
                final avr_irq_t irq = simavr.avr_io_getirq(avr, ctl, bit);
                simavr.avr_raise_irq(irq, (value.value >> bit) & 1);
            }
        }

        final int lastCpuState = cpuState;
        cpuState = simavr.avr_run(avr);
        if (cpuState != lastCpuState) {
            if (cpuState == simavr.cpu_Done) {
                System.err.println("cpuState = done");
            } else if (cpuState == simavr.cpu_Crashed) {
                System.err.println("cpuState = crashed");
            }
        }

        final Value txC = outputValues[0];
        final Value txD = outputValues[1];

        txC.highZMask = 0;
        txD.highZMask = 0x00;

        if (txC.value == 1)
            txC.value = 0;
        else if (!txQueue.isEmpty()) {
            txC.value = 1;
            txD.value = txQueue.remove() & 0xff;
        }

        for (int i = 0; i < mcu.ports.size(); i++) {
            final Mcu.Port port = mcu.ports.get(i);
            final Value value = outputValues[2 + i];
            final avr_ioport_state_t state = new avr_ioport_state_t();
            final long ctl = simavr.AVR_IOCTL_IOPORT_GETSTATE((short) port.name);
            simavr.avr_ioctl(avr, ctl, state.asVoidPointer());

            value.value = state.getPort() & value.bitMask;
            value.highZMask = ~state.getDdr() & value.bitMask;
        }
    }
}
