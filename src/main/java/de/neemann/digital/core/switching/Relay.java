package de.neemann.digital.core.switching;

import de.neemann.digital.core.*;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.element.PinDescription;
import de.neemann.digital.core.element.PinDescriptions;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.lang.Lang;

import static de.neemann.digital.core.element.PinInfo.input;

import java.util.ArrayList;

/**
 * A simple relay.
 * Created by hneemann on 22.02.17.
 */
public class Relay extends Node implements Element {

    /**
     * The switch description
     */
    public static final ElementTypeDescription DESCRIPTION = new RelayInDescription(Relay.class);
 
    private final boolean invers;
    private final boolean extraInput;
    private final Switch s;
    private ObservableValue input0;
    private ObservableValue input1;
    private boolean state;
    private boolean stateHighZ;

    /**
     * Create a new instance
     *
     * @param attr the attributes
     */
    public Relay(ElementAttributes attr) {
        this(attr, attr.get(Keys.RELAY_NORMALLY_CLOSED), attr.get(Keys.RELAY_EXTRA_INPUT));
    }

    /**
     * Create a new instance
     *
     * @param attr   the attributes
     * @param invers true if relay is closed on zero in.
     */
    public Relay(ElementAttributes attr, boolean invers,boolean extraInput) {
        this.invers = invers;
        this.extraInput = extraInput;
        s = new Switch(attr, invers);
    }

    @Override
    public ObservableValues getOutputs() throws PinException {
        return s.getOutputs();
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
    	int index=0; 
        input0 = inputs.get(index++).checkBits(1, this).addObserverToValue(this);
        if(this.extraInput) input1 = inputs.get(index++).checkBits(1, this).addObserverToValue(this);
        s.setInputs(new ObservableValues(inputs.get(index++), inputs.get(index++)));
    }

    @Override
    public void readInputs() throws NodeException {
    	if(this.extraInput){
            state = input0.getBool()^input1.getBool();
            stateHighZ = input0.isHighZ()|input1.isHighZ();    		    		
    	} else {
            state = input0.getBool();
            stateHighZ = input0.isHighZ();    		
    	}
    }

    @Override
    public void writeOutputs() throws NodeException {
        s.setClosed(getClosed(state, stateHighZ));
    }

    /**
     * get the closed state
     *
     * @param inState the input state
     * @param inHighZ input high z value
     * @return true if switch is to close
     */
    protected boolean getClosed(boolean inState, boolean inHighZ) {
        if (inHighZ)
            return invers;

        return inState ^ invers;
    }

    @Override
    public void init(Model model) throws NodeException {
        s.init(model);
    }

    /**
     * @return output 1
     */
    protected ObservableValue getOutput1() {
        return s.getOutput1();
    }

    /**
     * @return output 2
     */
    protected ObservableValue getOutput2() {
        return s.getOutput2();
    }

    /**
     * @return true is switch is closed
     */
    public boolean isClosed() {
        return s.isClosed();
    }
    
    
    /**
     * The relay in description
     */
    static class RelayInDescription extends ElementTypeDescription {
    	RelayInDescription(Class<? extends Element> clazz) {
            super(clazz);
            addAttributes();
        }

        private void addAttributes() {
            addAttribute(Keys.ROTATE);
            addAttribute(Keys.BITS);
            addAttribute(Keys.LABEL);
            addAttribute(Keys.RELAY_NORMALLY_CLOSED);
            addAttribute(Keys.RELAY_EXTRA_INPUT);
        }

        @Override
        public PinDescriptions getInputDescription(ElementAttributes elementAttributes) {
        	int count;
        	if(elementAttributes.get(Keys.RELAY_EXTRA_INPUT)) count=2;
        	else count=1;
            PinDescription[] names = new PinDescription[count];
            for (int i = 0; i < count; i++){
                names[i] = input("in" + (i+1), Lang.get("elem_Relay_pin_in" + (i+1)));
            }
            return new PinDescriptions(names);
        }
    }   
}
