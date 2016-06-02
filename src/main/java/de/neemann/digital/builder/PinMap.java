package de.neemann.digital.builder;

import de.neemann.digital.lang.Lang;

import java.util.*;

/**
 * A PinMap.
 * Used to assign a symbolic name to a pin number
 *
 * @author hneemann
 */
public class PinMap {
    private final HashMap<String, Integer> pinMap;
    private int[] inputPins;
    private int[] outputPins;

    /**
     * Creates a new instance
     */
    public PinMap() {
        pinMap = new HashMap<>();
    }

    /**
     * Sets the available input pin numbers
     *
     * @param inputPins the input pins
     * @return this for chained calls
     */
    public PinMap setAvailInputs(int... inputPins) {
        this.inputPins = inputPins;
        return this;
    }

    /**
     * Sets the available output pin numbers
     *
     * @param outputPins the input pins
     * @return this for chained calls
     */
    public PinMap setAvailOutputs(int... outputPins) {
        this.outputPins = outputPins;
        return this;
    }

    /**
     * Assign a symbolic name to a pin
     *
     * @param name the name
     * @param pin  the pin
     * @return this for chained calls
     * @throws PinMapException FuseMapFillerException
     */
    public PinMap assignPin(String name, int pin) throws PinMapException {
        if (name == null || name.length() == 0)
            throw new PinMapException(Lang.get("err_pinMap_NoNameForPin_N", pin));
        if (pinMap.containsKey(name))
            throw new PinMapException(Lang.get("err_pinMap_Pin_N_AssignedTwicerPin", name));
        if (pinMap.containsValue(pin))
            throw new PinMapException(Lang.get("err_pinMap_Pin_N_AssignedTwicerPin", pin));
        pinMap.put(name, pin);
        return this;
    }

    /**
     * Assigns pins to names.
     * Strings must have a form of "a=5, Q_0=6"
     *
     * @param assignment the assignment string
     * @return this for chained calls
     * @throws PinMapException PinMapException
     */
    public PinMap parseString(String assignment) throws PinMapException {
        StringTokenizer st = new StringTokenizer(assignment, ";,");
        while (st.hasMoreTokens()) {
            String tok = st.nextToken();
            int p = tok.indexOf("=");
            if (p < 0) throw new PinMapException(Lang.get("err_pinMap_noEqualsfound"));

            String name = tok.substring(0, p).trim();
            String numStr = tok.substring(p + 1).trim();
            try {
                int num = Integer.parseInt(numStr);
                assignPin(name, num);
            } catch (NumberFormatException e) {
                throw new PinMapException(e);
            }
        }
        return this;
    }


    private Integer search(int[] pins, String name) {
        for (int i : pins) {
            if (!pinMap.containsValue(i)) {
                pinMap.put(name, i);
                return i;
            }
        }
        return null;
    }

    private boolean contains(int[] pins, int p) {
        for (int i : pins)
            if (i == p) return true;
        return false;
    }

    /**
     * Gets the input pin number for the symbolic name.
     * If no assignment found on of the pins is selected automatically
     *
     * @param in the name
     * @return the  pin number
     * @throws PinMapException PinMap
     */
    public int getInputFor(String in) throws PinMapException {
        Integer p = pinMap.get(in);
        if (p == null)
            p = search(inputPins, in);
        if (p == null) {
            throw new PinMapException(Lang.get("err_pinMap_toMannyInputsDefined"));
        } else if (!contains(inputPins, p)) {
            throw new PinMapException(Lang.get("err_pinMap_input_N_notAllowed", p));
        }
        return p;
    }

    /**
     * Gets the output pin number for the symbolic name.
     * If no assignment is found on of the pins is selected automatically
     *
     * @param out the name
     * @return the  pin number
     * @throws PinMapException FuseMapFillerException
     */
    public int getOutputFor(String out) throws PinMapException {
        Integer p = pinMap.get(out);
        if (p == null)
            p = search(outputPins, out);
        if (p == null) {
            throw new PinMapException(Lang.get("err_pinMap_toMannyOutputsDefined"));
        } else if (!contains(outputPins, p)) {
            throw new PinMapException(Lang.get("err_pinMap_output_N_notAllowed", p));
        }
        return p;
    }

    /**
     * Returns a list of unused output pins
     *
     * @return the list
     */
    public List<Integer> getUnusedOutputs() {
        ArrayList<Integer> uo = new ArrayList<>();
        for (int i : outputPins)
            if (!pinMap.containsValue(i))
                uo.add(i);
        return uo;
    }

    @Override
    public String toString() {
        HashMap<Integer, String> revMap = new HashMap<>();
        for (Map.Entry<String, Integer> i : pinMap.entrySet())
            revMap.put(i.getValue(), i.getKey());


        StringBuilder sb = new StringBuilder();
        sb.append(Lang.get("msg_pinMap_inputs")).append(":\n");
        for (int i : inputPins)
            sb.append(Lang.get("msg_pinMap_pin_N_is_N", i, checkName(revMap.get(i)))).append("\n");


        sb.append("\n").append(Lang.get("msg_pinMap_outputs")).append(":\n");
        for (int i : outputPins)
            sb.append(Lang.get("msg_pinMap_pin_N_is_N", i, checkName(revMap.get(i)))).append("\n");

        return sb.toString();
    }

    private String checkName(String s) {
        if (s == null) return Lang.get("msg_pinMap_notUsed");
        return s;
    }
}
