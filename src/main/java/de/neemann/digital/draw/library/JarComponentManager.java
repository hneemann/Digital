/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.library;

import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.draw.shapes.ShapeFactory;
import de.neemann.digital.lang.Lang;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * Used to attach custom components
 */
public class JarComponentManager implements ComponentManager, Iterable<JarComponentManager.AdditionalShape> {
    private final ElementLibrary library;
    private final ArrayList<AdditionalShape> additionalShapes;

    JarComponentManager(ElementLibrary library) {
        this.library = library;
        additionalShapes = new ArrayList<>();
    }

    @Override
    public void addComponent(String nodePath, ElementTypeDescription description) throws InvalidNodeException {
        library.findNode(nodePath).add(description);
    }

    @Override
    public void addComponent(String nodePath, ElementTypeDescription description, ShapeFactory.Creator shape) throws InvalidNodeException {
        library.findNode(nodePath).add(description);
        additionalShapes.add(new AdditionalShape(description, shape));
    }

    @Override
    public Iterator<AdditionalShape> iterator() {
        return additionalShapes.iterator();
    }


    /**
     * Loads the components from a jar file
     *
     * @param file the jar file
     * @throws IOException          IOException
     * @throws InvalidNodeException InvalidNodeException
     */
    public void loadJar(File file) throws IOException, InvalidNodeException {
        Manifest manifest;
        try (JarFile jarFile = new JarFile(file)) {
            manifest = jarFile.getManifest();
        }
        if (manifest == null)
            throw new IOException(Lang.get("err_noManifestFound"));
        Attributes attr = manifest.getMainAttributes();
        String main = attr.getValue("Main-Class");
        if (main == null)
            throw new IOException(Lang.get("err_noMainFoundInManifest"));

        URLClassLoader cl = new URLClassLoader(new URL[]{file.toURI().toURL()});

        try {
            Class<?> c = cl.loadClass(main);
            ComponentSource cs = (ComponentSource) c.newInstance();
            cs.registerComponents(this);
        } catch (ClassNotFoundException e) {
            throw new IOException(Lang.get("err_mainClass_N_NotFound", main));
        } catch (IllegalAccessException | InstantiationException e) {
            throw new IOException(Lang.get("err_couldNotInitializeMainClass_N", main));
        }
    }

    /**
     * A additional shape
     */
    public static class AdditionalShape {
        private final ElementTypeDescription description;
        private final ShapeFactory.Creator shape;

        AdditionalShape(ElementTypeDescription description, ShapeFactory.Creator shape) {
            this.description = description;
            this.shape = shape;
        }

        /**
         * @return the description of the component
         */
        public ElementTypeDescription getDescription() {
            return description;
        }

        /**
         * @return the shape to use
         */
        public ShapeFactory.Creator getShape() {
            return shape;
        }
    }
}
