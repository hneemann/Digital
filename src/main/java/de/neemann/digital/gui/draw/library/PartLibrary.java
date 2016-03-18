package de.neemann.digital.gui.draw.library;

import de.neemann.digital.core.basic.*;
import de.neemann.digital.core.io.In;
import de.neemann.digital.core.io.Out;
import de.neemann.digital.core.part.PartTypeDescription;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * @author hneemann
 */
public class PartLibrary implements Iterable<PartLibrary.PartContainer> {

    private final HashMap<String, PartTypeDescription> map = new HashMap<>();
    private ArrayList<PartContainer> list = new ArrayList<>();

    public PartLibrary() {
        add(And.DESCRIPTION, "Logic");
        add(Or.DESCRIPTION, "Logic");
        add(NAnd.DESCRIPTION, "Logic");
        add(NOr.DESCRIPTION, "Logic");
        add(Not.DESCRIPTION, "Logic");

        add(In.DESCRIPTION, "IO");
        add(Out.DESCRIPTION, "IO");

//        add(Add.DESCRIPTION,"Aritmetic");
//        add(Sub.DESCRIPTION,"Aritmetic");
//        add(Mul.DESCRIPTION,"Aritmetic");
    }

    private void add(PartTypeDescription description, String treePath) {
        String name = description.getName();
        if (map.containsKey(name))
            throw new RuntimeException("duplicate part " + name);

        map.put(name, description);

        list.add(new PartContainer(name, treePath));
    }

    public PartTypeDescription getPartType(String partName) {
        PartTypeDescription pd = map.get(partName);
        if (pd == null)
            throw new RuntimeException("part " + partName + " not found");
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
