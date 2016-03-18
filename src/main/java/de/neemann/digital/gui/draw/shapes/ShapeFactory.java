package de.neemann.digital.gui.draw.shapes;

import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.basic.*;
import de.neemann.digital.core.io.In;
import de.neemann.digital.core.io.Out;
import de.neemann.digital.core.part.AttributeKey;
import de.neemann.digital.core.part.PartAttributes;
import de.neemann.digital.core.part.PartTypeDescription;

import java.util.HashMap;

/**
 * @author hneemann
 */
public final class ShapeFactory {
    public static final ShapeFactory INSTANCE = new ShapeFactory();
    public HashMap<String, Creator> map = new HashMap<>();

    private ShapeFactory() {
        map.put(And.DESCRIPTION.getName(), new CreatorSimple("&", And.DESCRIPTION, false));
        map.put(Or.DESCRIPTION.getName(), new CreatorSimple("\u22651", Or.DESCRIPTION, false));
        map.put(NAnd.DESCRIPTION.getName(), new CreatorSimple("&", NAnd.DESCRIPTION, true));
        map.put(NOr.DESCRIPTION.getName(), new CreatorSimple("\u22651", NOr.DESCRIPTION, true));
        map.put(Not.DESCRIPTION.getName(), new CreatorSimple("", Not.DESCRIPTION, true));

        map.put(In.DESCRIPTION.getName(), attributes -> new InputShape(attributes.get(AttributeKey.Bits)));
        map.put(Out.DESCRIPTION.getName(), attributes -> new OutputShape(attributes.get(AttributeKey.Bits)));
    }

    private String[] outputNames(PartTypeDescription description, PartAttributes attributes) {
        ObservableValue[] o = description.createPart(attributes).getOutputs();
        String[] names = new String[o.length];
        for (int i = 0; i < names.length; i++)
            names[i] = o[i].getName();
        return names;
    }

    public Shape getShape(String partName, PartAttributes partAttributes) {
        Creator cr = map.get(partName);
        if (cr == null)
            throw new RuntimeException("no shape for " + partName);

        return cr.create(partAttributes);
    }

    private interface Creator {
        Shape create(PartAttributes attributes);
    }

    public class CreatorSimple implements Creator {

        private final String name;
        private final PartTypeDescription description;
        private final boolean invers;

        public CreatorSimple(String name, PartTypeDescription description, boolean invers) {
            this.name = name;
            this.description = description;
            this.invers = invers;
        }

        @Override
        public Shape create(PartAttributes attributes) {
            return new GenericShape(name, description.getInputNames(attributes), outputNames(description, attributes)).invert(invers);
        }
    }
}
