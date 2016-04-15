package de.neemann.digital.draw.library;

import de.neemann.digital.core.arithmetic.*;
import de.neemann.digital.core.basic.*;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.flipflops.D_FF;
import de.neemann.digital.core.flipflops.JK_FF;
import de.neemann.digital.core.flipflops.RS_FF;
import de.neemann.digital.core.flipflops.T_FF;
import de.neemann.digital.core.io.*;
import de.neemann.digital.core.memory.*;
import de.neemann.digital.core.wiring.*;
import de.neemann.digital.gui.components.data.DummyElement;
import de.neemann.digital.gui.components.terminal.Terminal;
import de.neemann.digital.lang.Lang;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * @author hneemann
 */
public class ElementLibrary implements Iterable<ElementLibrary.ElementContainer> {

    private final HashMap<String, ElementTypeDescription> map = new HashMap<>();
    private ArrayList<ElementContainer> list = new ArrayList<>();
    private ElementNotFoundNotification elementNotFoundNotification;

    /**
     * Creates a new instance.
     */
    public ElementLibrary() {
        String menu = Lang.get("lib_Logic");
        add(And.DESCRIPTION, menu);
        add(NAnd.DESCRIPTION, menu);
        add(Or.DESCRIPTION, menu);
        add(NOr.DESCRIPTION, menu);
        add(XOr.DESCRIPTION, menu);
        add(XNOr.DESCRIPTION, menu);
        add(Not.DESCRIPTION, menu);
        add(LookUpTable.DESCRIPTION, menu);

        menu = Lang.get("lib_io");
        add(In.DESCRIPTION, menu);
        add(Out.DESCRIPTION, menu);
        add(Out.LEDDESCRIPTION, menu);
        add(Button.DESCRIPTION, menu);
        add(Probe.DESCRIPTION, menu);
        add(Out.SEVENDESCRIPTION, menu);
        add(Out.SEVENHEXDESCRIPTION, menu);
        add(Terminal.DESCRIPTION, menu);
        add(DummyElement.DATADESCRIPTION, menu);
        add(DummyElement.TEXTDESCRIPTION, menu);

        menu = Lang.get("lib_mux");
        add(Multiplexer.DESCRIPTION, menu);
        add(Demultiplexer.DESCRIPTION, menu);
        add(Decoder.DESCRIPTION, menu);

        menu = Lang.get("lib_wires");
        add(Const.DESCRIPTION, menu);
        add(Splitter.DESCRIPTION, menu);
        add(Clock.DESCRIPTION, menu);
        add(Delay.DESCRIPTION, menu);
        add(Driver.DESCRIPTION, menu);
        add(Reset.DESCRIPTION, menu);
        add(Break.DESCRIPTION, menu);

        menu = Lang.get("lib_flipFlops");
        add(RS_FF.DESCRIPTION, menu);
        add(JK_FF.DESCRIPTION, menu);
        add(D_FF.DESCRIPTION, menu);
        add(T_FF.DESCRIPTION, menu);

        menu = Lang.get("lib_memory");
        add(Register.DESCRIPTION, menu);
        add(ROM.DESCRIPTION, menu);
        add(RAMDualPort.DESCRIPTION, menu);
        add(RAMSinglePort.DESCRIPTION, menu);
        add(Counter.DESCRIPTION, menu);

        menu = Lang.get("lib_arithmetic");
        add(Add.DESCRIPTION, menu);
        add(Sub.DESCRIPTION, menu);
        add(Mul.DESCRIPTION, menu);
        add(Comparator.DESCRIPTION, menu);
        add(Neg.DESCRIPTION, menu);
    }

    private void add(ElementTypeDescription description, String treePath) {
        addDescription(description);
        list.add(new ElementContainer(description, treePath));
    }

    /**
     * Adds a description to the library
     *
     * @param description the descritpion
     */
    public void addDescription(ElementTypeDescription description) {
        String name = description.getName();
        if (map.containsKey(name))
            throw new RuntimeException(Lang.get("err_duplicateElement_N", name));

        map.put(name, description);
    }

    /**
     * Returns a {@link ElementTypeDescription} by a given name.
     * If not found its tryed to load it.
     *
     * @param elementName the elements name
     * @return the {@link ElementTypeDescription} ore null if not found
     */
    public ElementTypeDescription getElementType(String elementName) {
        ElementTypeDescription pd = map.get(elementName);
        if (pd == null) {
            if (elementNotFoundNotification != null)
                pd = elementNotFoundNotification.elementNotFound(elementName);
            if (pd == null)
                throw new RuntimeException(Lang.get("err_element_N_notFound", elementName));
        }
        return pd;
    }

    @Override
    public Iterator<ElementContainer> iterator() {
        return list.iterator();
    }

    /**
     * Setes the {@link ElementNotFoundNotification} which can be calle if a element is not present.
     *
     * @param elementNotFoundNotification elementNotFoundNotification
     */
    public void setElementNotFoundNotification(ElementNotFoundNotification elementNotFoundNotification) {
        this.elementNotFoundNotification = elementNotFoundNotification;
    }

    /**
     * Removes an element from the library
     *
     * @param name the elements name
     */
    public void removeElement(String name) {
        map.remove(name);
    }

    /**
     * Used to store a elements name and its position in the elements menu.
     */
    public static class ElementContainer {
        private final ElementTypeDescription name;
        private final String treePath;

        /**
         * Creates anew instance
         *  @param typeDescription     the elements typeDescription
         * @param treePath the elements menu path
         */
        public ElementContainer(ElementTypeDescription typeDescription, String treePath) {
            this.name = typeDescription;
            this.treePath = treePath;
        }

        /**
         * @return the elements name
         */
        public ElementTypeDescription getDescription() {
            return name;
        }

        /**
         * @return Returns the path in the menu
         */
        public String getTreePath() {
            return treePath;
        }
    }
}
