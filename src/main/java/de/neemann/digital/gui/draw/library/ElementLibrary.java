package de.neemann.digital.gui.draw.library;

import de.neemann.digital.core.arithmetic.Add;
import de.neemann.digital.core.arithmetic.Mul;
import de.neemann.digital.core.arithmetic.Sub;
import de.neemann.digital.core.basic.*;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.flipflops.D_FF;
import de.neemann.digital.core.flipflops.JK_FF;
import de.neemann.digital.core.flipflops.RS_FF;
import de.neemann.digital.core.flipflops.T_FF;
import de.neemann.digital.core.io.Const;
import de.neemann.digital.core.io.In;
import de.neemann.digital.core.io.Out;
import de.neemann.digital.core.wiring.Clock;
import de.neemann.digital.core.wiring.Delay;
import de.neemann.digital.core.wiring.Splitter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * @author hneemann
 */
public class ElementLibrary implements Iterable<ElementLibrary.PartContainer> {

    private final HashMap<String, ElementTypeDescription> map = new HashMap<>();
    private ArrayList<PartContainer> list = new ArrayList<>();

    public ElementLibrary() {
        add(And.DESCRIPTION, "Logic");
        add(NAnd.DESCRIPTION, "Logic");
        add(Or.DESCRIPTION, "Logic");
        add(NOr.DESCRIPTION, "Logic");
        add(XOr.DESCRIPTION, "Logic");
        add(XNOr.DESCRIPTION, "Logic");
        add(Not.DESCRIPTION, "Logic");
        add(Delay.DESCRIPTION, "Logic");

        add(In.DESCRIPTION, "IO");
        add(Out.DESCRIPTION, "IO");
        add(Out.LEDDESCRIPTION, "IO");
        add(Out.PROBEDESCRIPTION, "IO");
        add(Clock.DESCRIPTION, "IO");

        add(Splitter.DESCRIPTION, "Wires");
        add(Const.DESCRIPTION, "Wires");

        add(RS_FF.DESCRIPTION, "FlipFlops");
        add(JK_FF.DESCRIPTION, "FlipFlops");
        add(D_FF.DESCRIPTION, "FlipFlops");
        add(T_FF.DESCRIPTION, "FlipFlops");

        add(Add.DESCRIPTION, "Aritmetic");
        add(Sub.DESCRIPTION, "Aritmetic");
        add(Mul.DESCRIPTION, "Aritmetic");
    }

    private void add(ElementTypeDescription description, String treePath) {
        String name = description.getName();
        if (map.containsKey(name))
            throw new RuntimeException("duplicate element " + name);

        map.put(name, description);

        list.add(new PartContainer(name, treePath));
    }

    public ElementTypeDescription getElementType(String elementName) {
        ElementTypeDescription pd = map.get(elementName);
        if (pd == null)
            throw new RuntimeException("element " + elementName + " not found");
        return pd;
    }

    @Override
    public Iterator<PartContainer> iterator() {
        return list.iterator();
    }

    public static class PartContainer {
        private final String name;
        private final String treePath;

        public PartContainer(String name, String treePath) {
            this.name = name;
            this.treePath = treePath;
        }

        public String getName() {
            return name;
        }

        public String getTreePath() {
            return treePath;
        }
    }
}
