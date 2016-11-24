package de.neemann.digital.builder;

import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.analyse.expression.Variable;
import de.neemann.digital.core.Model;
import de.neemann.digital.core.Signal;
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
    private ArrayList<HashSet<String>> alias;

    /**
     * Creates a new instance
     */
    public PinMap() {
        pinMap = new HashMap<>();
        alias = new ArrayList<>();
    }

    /**
     * Reads the pin assignments from the given model
     *
     * @param model the model
     * @return this for chained calls
     * @throws PinMapException PinMapException
     */
    public PinMap addModel(Model model) throws PinMapException {
        for (Signal p : model.getInputs())
            addSignal(p);
        for (Signal p : model.getOutputs())
            addSignal(p);

        return this;
    }

    private void addSignal(Signal signal) throws PinMapException {
        if (signal.getDescription() != null && signal.getDescription().length()>0) {
            StringTokenizer st = new StringTokenizer(signal.getDescription(), "\n\r");
            while (st.hasMoreTokens()) {
                String line = st.nextToken();
                if (line.toLowerCase().startsWith("pin ")) {
                    String intStr = line.substring(4).trim();
                    try {
                        int pin = Integer.parseInt(intStr);
                        assignPin(signal.getName(), pin);
                        return;
                    } catch (NumberFormatException e) {
                        throw new PinMapException("invalid assignment " + signal.getName() + "=" + intStr);
                    }
                }
            }
        }
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
        if (assignment == null)
            return this;

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

    /**
     * returns true id the expression is a simple variable
     * Checks if the assignment is a simple A=B. If true an alias for A is generated in the pin map.
     * This is needed to void to assign two pins to the same logical signal.
     *
     * @param name       the name of the target
     * @param expression the expression to check
     * @return true if expression is a simple variable
     */
    public boolean isSimpleAlias(String name, Expression expression) {
        if (expression instanceof Variable) {
            String al = ((Variable) expression).getIdentifier();

            HashSet<String> found = null;
            for (HashSet<String> s : alias)
                if (s.contains(name) || s.contains(al)) {
                    found = s;
                    break;
                }

            if (found == null) {
                found = new HashSet<>();
                alias.add(found);
            }

            found.add(name);
            found.add(al);

            return true;
        }
        return false;
    }

    /**
     * Adds the given pin assignments to this pin map
     *
     * @param pinMap the given assignments
     * @return this for chained calls
     * @throws PinMapException PinMapException
     */
    public PinMap addAll(PinMap pinMap) throws PinMapException {
        if (pinMap != null)
            for (Map.Entry<String, Integer> e : pinMap.pinMap.entrySet())
                assignPin(e.getKey(), e.getValue());
        return this;
    }

    private Integer searchFirstFreePin(int[] pins, String name) {
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
        Integer p = searchPinWithAlias(in);
        if (p == null)
            p = searchFirstFreePin(inputPins, in);
        if (p == null) {
            throw new PinMapException(Lang.get("err_pinMap_toMannyInputsDefined"));
        } else if (!contains(inputPins, p)) {
            throw new PinMapException(Lang.get("err_pinMap_input_N_notAllowed", p));
        }
        return p;
    }

    private Integer searchPinWithAlias(String pinName) {
        for (HashSet<String> aliasSet : alias)
            if (aliasSet.contains(pinName)) { // the are aliases
                for (String n : aliasSet) {
                    Integer p = pinMap.get(n);
                    if (p != null)
                        return p;
                }
            }
        return pinMap.get(pinName);
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
        Integer p = searchPinWithAlias(out);
        if (p == null)
            p = searchFirstFreePin(outputPins, out);
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
