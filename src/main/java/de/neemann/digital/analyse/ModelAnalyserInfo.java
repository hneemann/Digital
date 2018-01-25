package de.neemann.digital.analyse;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.Signal;

import java.util.ArrayList;
import java.util.TreeMap;

/**
 * Additional infos obtained from the model
 */
public class ModelAnalyserInfo {
    private final String clockPin;
    private final TreeMap<String, String> pins;
    private final ArrayList<Signal> inputs;
    private final ArrayList<Signal> outputs;
    private ArrayList<String> pinsWithoutNumber;

    /**
     * creates a new instance
     *
     * @param model   the model used
     * @param inputs  input singnales
     * @param outputs output signals
     */
    ModelAnalyserInfo(Model model, ArrayList<Signal> inputs, ArrayList<Signal> outputs) {
        this.inputs = inputs;
        this.outputs = outputs;
        pins = new TreeMap<>();

        if (model.getClocks().size() == 1)
            clockPin = model.getClocks().get(0).getClockPin();
        else
            clockPin = null;
    }

    /**
     * Adds the signals pin number to the table
     *
     * @param s the signal
     * @throws NodeException NodeException
     */
    public void addPinNumber(Signal s) throws NodeException {
        String p = s.getPinNumber();
        if (p != null && p.length() > 0) pins.put(s.getName(), p);
    }

    /**
     * @return the assigned pins
     */
    public TreeMap<String, String> getPins() {
        return pins;
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
    public String getClockPin() {
        return clockPin;
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

}
