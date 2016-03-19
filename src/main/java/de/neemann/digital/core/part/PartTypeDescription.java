package de.neemann.digital.core.part;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

/**
 * One instance for a part, so there is only one PartDescription for an AND.
 * Regardless of how many of these parts are used in the circuit.
 * It has the possibility to create a concrete part by using the given factory
 *
 * @author hneemann
 */
public class PartTypeDescription {
    private final String name;
    private final PartFactory partFactory;
    private final String[] inputNames;
    private final ArrayList<AttributeKey> attributeList;

    public PartTypeDescription(Class<?> clazz, String... inputNames) {
        this(clazz.getSimpleName(), clazz, inputNames);
    }

    public PartTypeDescription(String name, Class<?> clazz, String... inputNames) {
        this(name, new PartFactory() {
            @Override
            public Part create(PartAttributes attributes) {
                try {
                    Constructor<?> constructor = clazz.getConstructor(PartAttributes.class);
                    return (Part) constructor.newInstance(attributes);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }, inputNames);
    }

    public PartTypeDescription(String name, PartFactory partFactory, String... inputNames) {
        this.name = name;
        this.partFactory = partFactory;
        this.inputNames = inputNames;
        attributeList = new ArrayList<>();
    }


    public <VALUE> PartTypeDescription addAttribute(AttributeKey<VALUE> key) {
        attributeList.add(key);
        return this;
    }

    public ArrayList<AttributeKey> getAttributeList() {
        return attributeList;
    }

    public String getName() {
        return name;
    }

    public String[] getInputNames(PartAttributes partAttributes) {
        return inputNames;
    }

    public Part createPart(PartAttributes partAttributes) {
        return partFactory.create(partAttributes);
    }
}
