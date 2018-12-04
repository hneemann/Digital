/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.shapes.custom.svg;

import de.neemann.digital.draw.graphics.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class TransformParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransformParser.class);

    private SVGTokenizer tok;

    /**
     * Creates a new instance
     *
     * @param transform the path to parse
     */
    TransformParser(String transform) {
        tok = new SVGTokenizer(transform);
    }

    /**
     * Parses the transformation
     *
     * @return the transformation
     */
    public Transform parse() {
        Transform combined = Transform.IDENTITY;
        try {
            Transform t;
            while (!tok.isEOF()) {
                final String command = tok.readCommand();
                switch (command) {
                    case "translate":
                        tok.expect('(');
                        final float x = tok.readFloat();
                        float y = 0;
                        if (tok.nextIsNumber())
                            y = tok.readFloat();
                        tok.expect(')');
                        t = new TransformTranslate(new VectorFloat(x, y));
                        break;
                    case "scale":
                        tok.expect('(');
                        final float xs = tok.readFloat();
                        float ys = xs;
                        if (tok.nextIsNumber())
                            ys = tok.readFloat();
                        tok.expect(')');
                        t = new TransformMatrix(xs, 0, 0, ys, 0, 0);
                        break;
                    case "matrix":
                        tok.expect('(');
                        final float ma = tok.readFloat();
                        final float mb = tok.readFloat();
                        final float mc = tok.readFloat();
                        final float md = tok.readFloat();
                        final float mx = tok.readFloat();
                        final float my = tok.readFloat();
                        tok.expect(')');
                        t = new TransformMatrix(
                                ma,
                                mc,
                                mb,
                                md,
                                mx,
                                my);
                        break;
                    case "rotate":
                        tok.expect('(');
                        float w = tok.readFloat();
                        if (tok.nextIsNumber()) {
                            t = TransformMatrix.rotate(w);
                            float xc = tok.readFloat();
                            float yc = tok.readFloat();
                            t = Transform.mul(new TransformTranslate(-xc, -yc), t);
                            t = Transform.mul(t, new TransformTranslate(xc, yc));
                        } else
                            t = TransformMatrix.rotate(w);
                        tok.expect(')');
                        break;
                    default:
                        throw new RuntimeException("unknown transform: " + command, null);
                }
                combined = Transform.mul(t, combined);
            }
        } catch (Exception e) {
            LOGGER.warn(tok + ": " + e.getMessage());
        }
        return combined;
    }

}
