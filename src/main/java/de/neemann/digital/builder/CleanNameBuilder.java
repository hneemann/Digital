/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.builder;

import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.analyse.expression.Variable;
import de.neemann.digital.analyse.expression.modify.ExpressionModifier;

import java.util.HashMap;

/**
 * Builder which performs a name cleanup
 */
public class CleanNameBuilder implements BuilderInterface<CleanNameBuilder> {

    private BuilderInterface parent;
    private final Filter filter;
    private final HashMap<String, String> nameMap;

    /**
     * Creates a new instance which allows only characters, numbers and the the underscore.
     *
     * @param parent the parent builder
     */
    public CleanNameBuilder(BuilderInterface parent) {
        this(parent, new SimpleFilter());
    }

    /**
     * Creates a ne instance
     *
     * @param parent the parent builder
     * @param filter the name filter to use
     */
    public CleanNameBuilder(BuilderInterface parent, Filter filter) {
        this.parent = parent;
        this.filter = filter;
        nameMap = new HashMap<>();
    }

    @Override
    public CleanNameBuilder addCombinatorial(String name, Expression expression) throws BuilderException {
        parent.addCombinatorial(checkName(name), checkName(expression));
        return this;
    }

    @Override
    public CleanNameBuilder addSequential(String name, Expression expression) throws BuilderException {
        parent.addSequential(checkName(name), checkName(expression));
        return this;
    }

    private Expression checkName(Expression expression) {
        return ExpressionModifier.modifyExpression(expression, exp -> {
            if (exp instanceof Variable) {
                return new Variable(checkName(((Variable) exp).getIdentifier()));
            } else
                return exp;
        });
    }

    private String checkName(String name) {
        String n = nameMap.get(name);
        if (n == null) {
            n = filter.filter(name);
            if (n == null || n.isEmpty())
                n = "X";
            if (nameMap.containsValue(n)) {
                int num = 1;
                while (nameMap.containsValue(n + num))
                    num++;
                n = n + num;
            }
            nameMap.put(name, n);
        }
        return n;
    }

    /**
     * Creates a pinMap which is consistent to the pin renaming done by this builder.
     *
     * @return the pin map
     */
    public PinMap createPinMap() {
        return new PinMap() {
            @Override
            public PinMap assignPin(String name, int pin) throws PinMapException {
                return super.assignPin(checkName(name), pin);
            }
        };
    }

    /**
     * Sets the parent builder.
     *
     * @param parent the parents builder
     */
    public void setParent(BuilderInterface parent) {
        this.parent = parent;
    }

    /**
     * Filter interface
     */
    public interface Filter {
        /**
         * Has to return a legal name
         *
         * @param name the eventually non legal name
         * @return the legal name
         */
        String filter(String name);
    }

    private static final class SimpleFilter implements Filter {
        private final CharacterFilter[] filters;

        public SimpleFilter() {
            filters = new CharacterFilter[]{ // <--
                    new RangeCharacterFilter('A', 'Z'),
                    new RangeCharacterFilter('a', 'z'),
                    new RangeCharacterFilter('0', '9'),
                    new SingleCharacterFilter('_')
            };
        }
        @Override
        public String filter(String name) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < name.length(); i++) {
                char c = name.charAt(i);
                for (CharacterFilter filter : filters) { // <--
                    if (filter.accept(c)) { // <--
                        sb.append(c);
                        break;
                    }
                }
            }

            return sb.toString();
        }

        private interface CharacterFilter {
            boolean accept(char c);
        }

        private static class RangeCharacterFilter implements CharacterFilter {
            private final char min;
            private final char max;

            public RangeCharacterFilter(char min, char max) {
                this.min = min;
                this.max = max;
            }

            @Override
            public boolean accept(char c) {
                return c >= min && c <= max;
            }
        }

        private static class SingleCharacterFilter implements CharacterFilter {
            private final char target;

            public SingleCharacterFilter(char target) {
                this.target = target;
            }

            @Override
            public boolean accept(char c) {
                return c == target;
            }
        }

    }
}
