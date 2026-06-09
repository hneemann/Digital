/*
 * Copyright (c) 2026 Jonas Meeuws
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.simavr;

import be.jnms.simavr.LibSimAVR;
import be.jnms.simavr.swig.avr_ioport_state_t;
import be.jnms.simavr.swig.avr_t;
import be.jnms.simavr.swig.elf_firmware_t;
import be.jnms.simavr.swig.simavr;
import de.neemann.digital.core.*;
import de.neemann.digital.core.element.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;

/**
 * The Simavr
 */
public class Simavr extends Node implements Element {
    private static final Logger LOGGER = LoggerFactory.getLogger(Simavr.class);

    private enum InputPin {
        PB0(0),
        PB1(1),
        PB2(2),
        PB3(3),
        PB4(4),
        PB5(5),
        PB6(6),
        PB7(7),
        PC0(8),
        PC1(9),
        PC2(10),
        PC3(11),
        PC4(12),
        PC5(13),
        PC6(14),
        PD0(15),
        PD1(16),
        PD2(17),
        PD3(18),
        PD4(19),
        PD5(20),
        PD6(21),
        PD7(22),
        Reset(23),
        Clock(24);

        public static final InputPin[] LUT = values();
        public static final int TOTAL_COUNT = LUT.length;

        public final int idx;

        InputPin(int idx) {
            this.idx = idx;
        }
    }

    private enum OutputPin {
        PB0(0),
        PB1(1),
        PB2(2),
        PB3(3),
        PB4(4),
        PB5(5),
        PB6(6),
        PB7(7),
        PC0(8),
        PC1(9),
        PC2(10),
        PC3(11),
        PC4(12),
        PC5(13),
        PC6(14),
        PD0(15),
        PD1(16),
        PD2(17),
        PD3(18),
        PD4(19),
        PD5(20),
        PD6(21),
        PD7(22);

        public static final OutputPin[] LUT = values();
        public static final int TOTAL_COUNT = LUT.length;

        public final int idx;

        OutputPin(int idx) {
            this.idx = idx;
        }
    }

    /**
     * The Simavr description
     */
    public static final ElementTypeDescription DESCRIPTION
        = new ElementTypeDescription(
                Simavr.class,
                Arrays.stream(InputPin.LUT)
                    .map(pin -> PinInfo.input(pin.name()))
                    .toArray(PinInfo[]::new)
            )
            .addAttribute(Keys.SIMAVR_MCU)
            .addAttribute(Keys.SIMAVR_FIRMWARE_FILE_PATH);

    @SuppressWarnings("unused")
    private static void onGlobalLogMessage(int level, String message) {
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

    private ElementAttributes attributes;

    private ObservableValue[] inputs;
    private boolean[] inputValues;

    private ObservableValue[] outputs;
    private boolean[] outputValues;

    private avr_t avr;
    private int cpuState;

    /**
     * Creates a new instance
     *
     * @param attributes the attributes
     */
    public Simavr(ElementAttributes attributes) {
        super(true);

        this.attributes = attributes;
        attributes.set(Keys.WIDTH, 5);

        inputs = new ObservableValue[InputPin.TOTAL_COUNT];
        inputValues = new boolean[InputPin.TOTAL_COUNT];
        for (int i = 0; i < InputPin.TOTAL_COUNT; i++) {
            inputValues[i] = false;
        }

        outputs = new ObservableValue[OutputPin.TOTAL_COUNT];
        outputValues = new boolean[OutputPin.TOTAL_COUNT];
        for (int i = 0; i < OutputPin.TOTAL_COUNT; i++) {
            outputs[i] = new ObservableValue(OutputPin.LUT[i].name(), 1);
            outputValues[i] = false;
        }

        avr = null;
        cpuState = 0;
    }

    @Override
    public void setInputs(ObservableValues newInputs) throws BitsException {
        for (int i = 0; i < InputPin.TOTAL_COUNT; i++) {
            inputs[i] = newInputs.get(i).addObserverToValue(this).checkBits(1, this, i);
        }
    }

    @Override
    public ObservableValues getOutputs() {
        return new ObservableValues(outputs);
    }

    @Override
    public void readInputs() throws NodeException {
        setupIfNeeded();

        final boolean lastResetValue = inputValues[InputPin.Reset.idx];
        final boolean lastClockValue = inputValues[InputPin.Clock.idx];

        for (int i = 0; i < InputPin.TOTAL_COUNT; i++) {
            inputValues[i] = inputs[i].getBool();
        }

        // Detect Reset rising edge
        if (inputValues[InputPin.Reset.idx] && !lastResetValue) {
            reset();
        }

        // Detect Clock rising edge
        if (inputValues[InputPin.Clock.idx] && !lastClockValue) {
            runOneCycle();
        }
    }

    @Override
    public void writeOutputs() throws NodeException {
        for (int i = 0; i < OutputPin.TOTAL_COUNT; i++) {
            outputs[i].setBool(outputValues[i]);
        }
    }

    private void setupIfNeeded() {
        if (avr == null) {
            setup();
        }
    }

    private void setup() {
        System.err.println("setup()");
        try {
            LibSimAVR.load();
        } catch (IOException e) {
            // TODO
            throw new IllegalStateException(e);
        }
        simavr.swigSetLoggerMethod(Simavr.class, "onGlobalLogMessage");

        elf_firmware_t elf = new elf_firmware_t();
        final String firmwareFilePath = attributes.get(Keys.SIMAVR_FIRMWARE_FILE_PATH).getPath();

        if (simavr.elf_read_firmware(firmwareFilePath, elf) != 0) {
            // TODO
            throw new IllegalArgumentException("failed to read " + firmwareFilePath);
        }

        avr = simavr.avr_make_mcu_by_name(attributes.get(Keys.SIMAVR_MCU));
        simavr.avr_init(avr);
        simavr.avr_load_firmware(avr, elf);
    }

    private void reset() {
        System.err.println("reset()");
        setup();
    }

    private void runOneCycle() {
        setupIfNeeded();

        if (inputValues[InputPin.Reset.idx])
            return;

        /*
        final int portBDriveValue = 0
                | (inputValues[InputPin.PB0.idx] ? 1 << 0 : 0)
                | (inputValues[InputPin.PB1.idx] ? 1 << 1 : 0)
                | (inputValues[InputPin.PB2.idx] ? 1 << 2 : 0)
                | (inputValues[InputPin.PB3.idx] ? 1 << 3 : 0)
                | (inputValues[InputPin.PB4.idx] ? 1 << 4 : 0)
                | (inputValues[InputPin.PB5.idx] ? 1 << 5 : 0)
                | (inputValues[InputPin.PB6.idx] ? 1 << 6 : 0)
                | (inputValues[InputPin.PB7.idx] ? 1 << 7 : 0);
        final int portCDriveValue = 0
                | (inputValues[InputPin.PC0.idx] ? 1 << 0 : 0)
                | (inputValues[InputPin.PC1.idx] ? 1 << 1 : 0)
                | (inputValues[InputPin.PC2.idx] ? 1 << 2 : 0)
                | (inputValues[InputPin.PC3.idx] ? 1 << 3 : 0)
                | (inputValues[InputPin.PC4.idx] ? 1 << 4 : 0)
                | (inputValues[InputPin.PC5.idx] ? 1 << 5 : 0)
                | (inputValues[InputPin.PC6.idx] ? 1 << 6 : 0);
        final int portDDriveValue = 0
                | (inputValues[InputPin.PD0.idx] ? 1 << 0 : 0)
                | (inputValues[InputPin.PD1.idx] ? 1 << 1 : 0)
                | (inputValues[InputPin.PD2.idx] ? 1 << 2 : 0)
                | (inputValues[InputPin.PD3.idx] ? 1 << 3 : 0)
                | (inputValues[InputPin.PD4.idx] ? 1 << 4 : 0)
                | (inputValues[InputPin.PD5.idx] ? 1 << 5 : 0)
                | (inputValues[InputPin.PD6.idx] ? 1 << 6 : 0)
                | (inputValues[InputPin.PD7.idx] ? 1 << 7 : 0);

        avr_ioport_external_t portBDrive = new avr_ioport_external_t();
        avr_ioport_external_t portCDrive = new avr_ioport_external_t();
        avr_ioport_external_t portDDrive = new avr_ioport_external_t();

        portBDrive.setMask((1 << 8) - 1);
        portCDrive.setMask((1 << 7) - 1);
        portDDrive.setMask((1 << 8) - 1);

        portBDrive.setValue(portBDriveValue);
        portCDrive.setValue(portCDriveValue);
        portDDrive.setValue(portDDriveValue);

        portBDrive.setValue(0);
        portCDrive.setValue(0);
        portDDrive.setValue(0);

        simavr.avr_ioctl(avr, simavr.AVR_IOCTL_IOPORT_SET_EXTERNAL_fn((short) 'B'), portBDrive.asVoidPointer());
        simavr.avr_ioctl(avr, simavr.AVR_IOCTL_IOPORT_SET_EXTERNAL_fn((short) 'C'), portCDrive.asVoidPointer());
        simavr.avr_ioctl(avr, simavr.AVR_IOCTL_IOPORT_SET_EXTERNAL_fn((short) 'D'), portDDrive.asVoidPointer());
        */

        simavr.avr_raise_irq(simavr.avr_io_getirq(avr, simavr.AVR_IOCTL_IOPORT_GETIRQ_fn((short) 'B'), 0), inputValues[InputPin.PB0.idx] ? 1 : 0);
        simavr.avr_raise_irq(simavr.avr_io_getirq(avr, simavr.AVR_IOCTL_IOPORT_GETIRQ_fn((short) 'B'), 1), inputValues[InputPin.PB1.idx] ? 1 : 0);
        simavr.avr_raise_irq(simavr.avr_io_getirq(avr, simavr.AVR_IOCTL_IOPORT_GETIRQ_fn((short) 'B'), 2), inputValues[InputPin.PB2.idx] ? 1 : 0);
        simavr.avr_raise_irq(simavr.avr_io_getirq(avr, simavr.AVR_IOCTL_IOPORT_GETIRQ_fn((short) 'B'), 3), inputValues[InputPin.PB3.idx] ? 1 : 0);
        simavr.avr_raise_irq(simavr.avr_io_getirq(avr, simavr.AVR_IOCTL_IOPORT_GETIRQ_fn((short) 'B'), 4), inputValues[InputPin.PB4.idx] ? 1 : 0);
        simavr.avr_raise_irq(simavr.avr_io_getirq(avr, simavr.AVR_IOCTL_IOPORT_GETIRQ_fn((short) 'B'), 5), inputValues[InputPin.PB5.idx] ? 1 : 0);
        simavr.avr_raise_irq(simavr.avr_io_getirq(avr, simavr.AVR_IOCTL_IOPORT_GETIRQ_fn((short) 'B'), 6), inputValues[InputPin.PB6.idx] ? 1 : 0);
        simavr.avr_raise_irq(simavr.avr_io_getirq(avr, simavr.AVR_IOCTL_IOPORT_GETIRQ_fn((short) 'B'), 7), inputValues[InputPin.PB7.idx] ? 1 : 0);
        simavr.avr_raise_irq(simavr.avr_io_getirq(avr, simavr.AVR_IOCTL_IOPORT_GETIRQ_fn((short) 'C'), 0), inputValues[InputPin.PC0.idx] ? 1 : 0);
        simavr.avr_raise_irq(simavr.avr_io_getirq(avr, simavr.AVR_IOCTL_IOPORT_GETIRQ_fn((short) 'C'), 1), inputValues[InputPin.PC1.idx] ? 1 : 0);
        simavr.avr_raise_irq(simavr.avr_io_getirq(avr, simavr.AVR_IOCTL_IOPORT_GETIRQ_fn((short) 'C'), 2), inputValues[InputPin.PC2.idx] ? 1 : 0);
        simavr.avr_raise_irq(simavr.avr_io_getirq(avr, simavr.AVR_IOCTL_IOPORT_GETIRQ_fn((short) 'C'), 3), inputValues[InputPin.PC3.idx] ? 1 : 0);
        simavr.avr_raise_irq(simavr.avr_io_getirq(avr, simavr.AVR_IOCTL_IOPORT_GETIRQ_fn((short) 'C'), 4), inputValues[InputPin.PC4.idx] ? 1 : 0);
        simavr.avr_raise_irq(simavr.avr_io_getirq(avr, simavr.AVR_IOCTL_IOPORT_GETIRQ_fn((short) 'C'), 5), inputValues[InputPin.PC5.idx] ? 1 : 0);
        simavr.avr_raise_irq(simavr.avr_io_getirq(avr, simavr.AVR_IOCTL_IOPORT_GETIRQ_fn((short) 'C'), 6), inputValues[InputPin.PC6.idx] ? 1 : 0);
        simavr.avr_raise_irq(simavr.avr_io_getirq(avr, simavr.AVR_IOCTL_IOPORT_GETIRQ_fn((short) 'D'), 0), inputValues[InputPin.PD0.idx] ? 1 : 0);
        simavr.avr_raise_irq(simavr.avr_io_getirq(avr, simavr.AVR_IOCTL_IOPORT_GETIRQ_fn((short) 'D'), 1), inputValues[InputPin.PD1.idx] ? 1 : 0);
        simavr.avr_raise_irq(simavr.avr_io_getirq(avr, simavr.AVR_IOCTL_IOPORT_GETIRQ_fn((short) 'D'), 2), inputValues[InputPin.PD2.idx] ? 1 : 0);
        simavr.avr_raise_irq(simavr.avr_io_getirq(avr, simavr.AVR_IOCTL_IOPORT_GETIRQ_fn((short) 'D'), 3), inputValues[InputPin.PD3.idx] ? 1 : 0);
        simavr.avr_raise_irq(simavr.avr_io_getirq(avr, simavr.AVR_IOCTL_IOPORT_GETIRQ_fn((short) 'D'), 4), inputValues[InputPin.PD4.idx] ? 1 : 0);
        simavr.avr_raise_irq(simavr.avr_io_getirq(avr, simavr.AVR_IOCTL_IOPORT_GETIRQ_fn((short) 'D'), 5), inputValues[InputPin.PD5.idx] ? 1 : 0);
        simavr.avr_raise_irq(simavr.avr_io_getirq(avr, simavr.AVR_IOCTL_IOPORT_GETIRQ_fn((short) 'D'), 6), inputValues[InputPin.PD6.idx] ? 1 : 0);
        simavr.avr_raise_irq(simavr.avr_io_getirq(avr, simavr.AVR_IOCTL_IOPORT_GETIRQ_fn((short) 'D'), 7), inputValues[InputPin.PD7.idx] ? 1 : 0);

        final int lastCpuState = cpuState;
        cpuState = simavr.avr_run(avr);
        if (cpuState != lastCpuState) {
            if (cpuState == simavr.cpu_Done) {
                System.err.println("cpuState = done");
            } else if (cpuState == simavr.cpu_Crashed) {
                System.err.println("cpuState = crashed");
            }
        }

        final avr_ioport_state_t portBState = new avr_ioport_state_t();
        final avr_ioport_state_t portCState = new avr_ioport_state_t();
        final avr_ioport_state_t portDState = new avr_ioport_state_t();

        simavr.avr_ioctl(avr, simavr.AVR_IOCTL_IOPORT_GETSTATE_fn((short) 'B'), portBState.asVoidPointer());
        simavr.avr_ioctl(avr, simavr.AVR_IOCTL_IOPORT_GETSTATE_fn((short) 'C'), portCState.asVoidPointer());
        simavr.avr_ioctl(avr, simavr.AVR_IOCTL_IOPORT_GETSTATE_fn((short) 'D'), portDState.asVoidPointer());

        outputValues[OutputPin.PB0.idx] = ((portBState.getPort() >> 0) & 1) == 1;
        outputValues[OutputPin.PB1.idx] = ((portBState.getPort() >> 1) & 1) == 1;
        outputValues[OutputPin.PB2.idx] = ((portBState.getPort() >> 2) & 1) == 1;
        outputValues[OutputPin.PB3.idx] = ((portBState.getPort() >> 3) & 1) == 1;
        outputValues[OutputPin.PB4.idx] = ((portBState.getPort() >> 4) & 1) == 1;
        outputValues[OutputPin.PB5.idx] = ((portBState.getPort() >> 5) & 1) == 1;
        outputValues[OutputPin.PB6.idx] = ((portBState.getPort() >> 6) & 1) == 1;
        outputValues[OutputPin.PB7.idx] = ((portBState.getPort() >> 7) & 1) == 1;

        outputValues[OutputPin.PC0.idx] = ((portCState.getPort() >> 0) & 1) == 1;
        outputValues[OutputPin.PC1.idx] = ((portCState.getPort() >> 1) & 1) == 1;
        outputValues[OutputPin.PC2.idx] = ((portCState.getPort() >> 2) & 1) == 1;
        outputValues[OutputPin.PC3.idx] = ((portCState.getPort() >> 3) & 1) == 1;
        outputValues[OutputPin.PC4.idx] = ((portCState.getPort() >> 4) & 1) == 1;
        outputValues[OutputPin.PC5.idx] = ((portCState.getPort() >> 5) & 1) == 1;
        outputValues[OutputPin.PC6.idx] = ((portCState.getPort() >> 6) & 1) == 1;

        outputValues[OutputPin.PD0.idx] = ((portDState.getPort() >> 0) & 1) == 1;
        outputValues[OutputPin.PD1.idx] = ((portDState.getPort() >> 1) & 1) == 1;
        outputValues[OutputPin.PD2.idx] = ((portDState.getPort() >> 2) & 1) == 1;
        outputValues[OutputPin.PD3.idx] = ((portDState.getPort() >> 3) & 1) == 1;
        outputValues[OutputPin.PD4.idx] = ((portDState.getPort() >> 4) & 1) == 1;
        outputValues[OutputPin.PD5.idx] = ((portDState.getPort() >> 5) & 1) == 1;
        outputValues[OutputPin.PD6.idx] = ((portDState.getPort() >> 6) & 1) == 1;
        outputValues[OutputPin.PD7.idx] = ((portDState.getPort() >> 7) & 1) == 1;

        // System.err.format("%02x%n", portBState.getPin());
        // System.err.format("%02x%n", portBState.getPort());
    }
}
