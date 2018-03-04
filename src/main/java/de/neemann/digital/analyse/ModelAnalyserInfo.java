/*
 * Copyright (c) 2018 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.Signal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

/**
 * Additional infos obtained from the model
 */
public class ModelAnalyserInfo {
    private final String clockPin;
    private final HashMap<String, ArrayList<String>> inputBusMap;
    private final HashMap<String, ArrayList<String>> outputBusMap;
    private TreeMap<String, String> pins;
    private ArrayList<Signal> inputs;
    private ArrayList<Signal> outputs;
    private ArrayList<String> pinsWithoutNumber;

    /**
     * creates a new instance
     *
     * @param model the model used
     */
    ModelAnalyserInfo(Model model) {
        if (model.getClocks().size() == 1)
            clockPin = model.getClocks().get(0).getClockPin();
        else
            clockPin = null;

        inputBusMap = new HashMap<>();
        outputBusMap = new HashMap<>();
    }

    void setInOut(ArrayList<Signal> inputs, ArrayList<Signal> outputs) {
        this.inputs = new ArrayList<>(inputs);
        this.outputs = new ArrayList<>(outputs);
    }

    /**
     * @return the assigned pins
     */
    public TreeMap<String, String> getPins() {
        if (pins == null) {
            pins = new TreeMap<>();
            for (Signal s : this.inputs)
                addPinNumber(s);
            for (Signal s : this.outputs)
                addPinNumber(s);
        }
        return pins;
    }

    private void addPinNumber(Signal s) {
        String p = s.getPinNumber();
        if (p != null && p.length() > 0) pins.put(s.getName(), p);
    }

    /**
     * @return list of pins without a number. Never null, maybe a empty list
     */
    public ArrayList<String> getPinsWithoutNumber() {
        if (pinsWithoutNumber == null) {
            pinsWithoutNumber = new ArrayList<>();
            for (Signal s : inputs)
                if (s.missingPinNumber())
                    pinsWithoutNumber.add((s.getName()));
            for (Signal s : outputs)
                if (s.missingPinNumber())
                    pinsWithoutNumber.add((s.getName()));
        }
        return pinsWithoutNumber;
    }

    /**
     * @return the clock pin
     */
    public int getClockPinInt() {
        if (clockPin == null || clockPin.length() == 0)
            return 0;

        try {
            return Integer.parseInt(clockPin);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    void addInputBus(String name, ArrayList<String> names) {
        inputBusMap.put(name, names);
    }

    void addOutputBus(String name, ArrayList<String> names) {
        outputBusMap.put(name, names);
    }

    /**
     * @return input bus map
     */
    public HashMap<String, ArrayList<String>> getInputBusMap() {
        return inputBusMap;
    }

    /**
     * @return output bus map
     */
    public HashMap<String, ArrayList<String>> getOutputBusMap() {
        return outputBusMap;
    }
}
