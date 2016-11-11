package de.neemann.digital.draw.library;

import de.neemann.digital.core.arithmetic.*;
import de.neemann.digital.core.basic.*;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.flipflops.FlipflopD;
import de.neemann.digital.core.flipflops.FlipflopJK;
import de.neemann.digital.core.flipflops.FlipflopRS;
import de.neemann.digital.core.flipflops.FlipflopT;
import de.neemann.digital.core.io.*;
import de.neemann.digital.core.memory.*;
import de.neemann.digital.core.pld.*;
import de.neemann.digital.core.wiring.*;
import de.neemann.digital.draw.elements.Tunnel;
import de.neemann.digital.gui.Main;
import de.neemann.digital.gui.components.data.DummyElement;
import de.neemann.digital.gui.components.graphics.GraphicCard;
import de.neemann.digital.gui.components.terminal.Keyboard;
import de.neemann.digital.gui.components.terminal.Terminal;
import de.neemann.digital.lang.Lang;
import de.neemann.digital.testing.TestCaseElement;

import java.io.File;
import java.io.IOException;
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
        add(Keyboard.DESCRIPTION, menu);

        menu = Lang.get("lib_mux");
        add(Multiplexer.DESCRIPTION, menu);
        add(Demultiplexer.DESCRIPTION, menu);
        add(Decoder.DESCRIPTION, menu);

        menu = Lang.get("lib_wires");
        add(Const.DESCRIPTION, menu);
        add(Splitter.DESCRIPTION, menu);
        add(Clock.DESCRIPTION, menu);
        add(Delay.DESCRIPTION, menu);
        add(Tunnel.DESCRIPTION, menu);
        add(Driver.DESCRIPTION, menu);
        add(DriverInvSel.DESCRIPTION, menu);
        add(Reset.DESCRIPTION, menu);
        add(Break.DESCRIPTION, menu);

        menu = Lang.get("lib_flipFlops");
        add(FlipflopRS.DESCRIPTION, menu);
        add(FlipflopJK.DESCRIPTION, menu);
        add(FlipflopD.DESCRIPTION, menu);
        add(FlipflopT.DESCRIPTION, menu);

        menu = Lang.get("lib_memory");
        add(Register.DESCRIPTION, menu);
        add(ROM.DESCRIPTION, menu);
        add(RAMDualPort.DESCRIPTION, menu);
        add(RAMSinglePort.DESCRIPTION, menu);
        add(GraphicCard.DESCRIPTION, menu);
        add(Counter.DESCRIPTION, menu);

        menu = Lang.get("lib_arithmetic");
        add(Add.DESCRIPTION, menu);
        add(Sub.DESCRIPTION, menu);
        add(Mul.DESCRIPTION, menu);
        add(Comparator.DESCRIPTION, menu);
        add(Neg.DESCRIPTION, menu);
        add(BitCount.DESCRIPTION, menu);

        menu = Lang.get("lib_pld");
        if (Main.enableExperimental())
            add(Diode.DESCRIPTION, menu);
        add(DiodeForeward.DESCRIPTION, menu);
        add(DiodeBackward.DESCRIPTION, menu);
        add(PullUp.DESCRIPTION, menu);
        add(PullDown.DESCRIPTION, menu);

        menu = Lang.get("lib_test");
        add(TestCaseElement.TESTCASEDESCRIPTION, menu);
    }

    private void add(ElementTypeDescription description, String treePath) {
        String name = description.getName();
        map.put(name, description);
        list.add(new ElementContainer(description, treePath));
    }

    /**
     * Adds a description to the library
     *
     * @param description the description
     * @param file        the file which was loaded
     */
    public void addDescription(ElementTypeDescription description, File file) {
        map.put(file.getName(), description);
    }


    /**
     * Returns a {@link ElementTypeDescription} by a given name.
     * If not found its tried to load it.
     *
     * @param elementName the elements name
     * @return the {@link ElementTypeDescription} ore null if not found
     * @throws ElementNotFoundException ElementNotFoundException
     */
    public ElementTypeDescription getElementType(String elementName) throws ElementNotFoundException {
        ElementTypeDescription description = map.get(elementName);
        if (description != null)
            return description;

        elementName = elementName.replace("\\", "/"); // effects only some old files!

        File file = new File(elementName);

        description = map.get(file.getName());
        if (description != null)
            return description;

        if (elementNotFoundNotification != null)
            try {
                description = elementNotFoundNotification.elementNotFound(file);
            } catch (IOException e) {
                throw new ElementNotFoundException(Lang.get("msg_errorImportingModel", elementName));
            }

        if (description != null)
            return description;

        throw new ElementNotFoundException(Lang.get("err_element_N_notFound", elementName));
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
    public void removeElement(File name) {
        map.remove(name.getName());
    }

    /**
     * Used to store a elements name and its position in the elements menu.
     */
    public static class ElementContainer {
        private final ElementTypeDescription name;
        private final String treePath;

        /**
         * Creates anew instance
         *
         * @param typeDescription the elements typeDescription
         * @param treePath        the elements menu path
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
