/*
 * Copyright (c) 2014 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.gui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Helper to create Images and Icons
 */
public final class IconCreator {
    private static final Logger LOGGER = LoggerFactory.getLogger(IconCreator.class);

    private IconCreator() {
    }

    /**
     * Creates an icon from a resource
     *
     * @param name name of the resource
     * @return the icon
     */
    public static Icon create(String name) {
        return new ImageIcon(createImage(name));
    }

    /**
     * Creates an image from a resource
     *
     * @param name name of the resource
     * @return the image
     */
    public static Image createImage(String name) {
        try {
            final float scaling = Screen.getInstance().getScaling();
            if (scaling == 1) {
                return getImage(name);
            } else {
                BufferedImage image = getImageOrNull(name.substring(0, name.length() - 4) + "_hi.png");
                if (image != null) {
                    int w = (int) (image.getWidth() * scaling / 2);
                    int h = (int) (image.getHeight() * scaling / 2);
                    return image.getScaledInstance(w, h, BufferedImage.SCALE_SMOOTH);
                } else {
                    LOGGER.info("upscaling of " + name);
                    image = getImage(name);
                    int w = (int) (image.getWidth() * scaling);
                    int h = (int) (image.getHeight() * scaling);
                    return image.getScaledInstance(w, h, BufferedImage.SCALE_SMOOTH);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Image " + name + " not found", e);
        }
    }

    private static BufferedImage getImage(String name) throws IOException {
        BufferedImage image = getImageOrNull(name);
        if (image == null) {
            throw new NullPointerException("resource " + name + " not found!");
        }
        return image;
    }

    private static BufferedImage getImageOrNull(String name) throws IOException {
        URL systemResource = ClassLoader.getSystemResource("icons/" + name);
        if (systemResource == null) {
            return null;
        }
        return ImageIO.read(systemResource);
    }

    /**
     * Creates an image list from a resource
     *
     * @param names names of the resource
     * @return the image
     */
    public static ArrayList<Image> createImages(String... names) {
        try {
            ArrayList<Image> list = new ArrayList<Image>(names.length);
            for (String name : names) {
                list.add(getImage(name));
            }
            return list;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
