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
import java.util.HashSet;

/**
 * Builder which performs a name cleanup
 */
public class CleanNameBuilder implements BuilderInterface<CleanNameBuilder> {

    private BuilderInterface parent;
    private final Filter filter;
    private final HashMap<String, String> nameMap;
    private final HashSet<String> namesUsedLower;

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
        namesUsedLower = new HashSet<>();
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
            String ln = n.toLowerCase();
            if (namesUsedLower.contains(ln)) {
                int num = 1;
                while (namesUsedLower.contains(ln + num))
                    num++;
                n = n + num;
            }
            nameMap.put(name, n);
            namesUsedLower.add(n.toLowerCase());
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

    /**
     * The SimpleFilter class applies multiple CharacterFilter instances to filter the input string.
     */
    private static final class SimpleFilter implements Filter {
        private final CharacterFilter[] filters;

        /**
         * Creates a SimpleFilter instance with a predefined set of character filters.
         */
         SimpleFilter() {
            filters = new CharacterFilter[]{
                    new RangeCharacterFilter('A', 'Z'),
                    new RangeCharacterFilter('a', 'z'),
                    new RangeCharacterFilter('0', '9'),
                    new SingleCharacterFilter('_')
            };
        }

        /**
         * Filters the input string by applying the CharacterFilter instances.
         *
         * @param name the input string to filter
         * @return the filtered string containing only accepted characters
         */
        @Override
        public String filter(String name) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < name.length(); i++) {
                char c = name.charAt(i);
                for (CharacterFilter filter : filters) {
                    if (filter.accept(c)) {
                        sb.append(c);
                        break;
                    }
                }
            }

            return sb.toString();
        }

        /**
         * The CharacterFilter interface defines a method to check if a character is accepted by the filter.
         */
        private interface CharacterFilter {
            /**
             * Checks if the given character is accepted by the filter.
             *
             * @param c the character to check
             * @return true if the character is accepted, false otherwise
             */
            boolean accept(char c);
        }

        /**
         * The RangeCharacterFilter class accepts characters within a specified range.
         */
        private static class RangeCharacterFilter implements CharacterFilter {
            private final char min;
            private final char max;

            /**
             * Creates a RangeCharacterFilter instance with a specified character range.
             *
             * @param min the minimum character in the range (inclusive)
             * @param max the maximum character in the range (inclusive)
             */
            RangeCharacterFilter(char min, char max) {
                this.min = min;
                this.max = max;
            }

            /**
             * Checks if the given character is within the specified range.
             *
             * @param c the character to check
             * @return true if the character is within the range, false otherwise
             */
            @Override
            public boolean accept(char c) {
                return c >= min && c <= max;
            }
        }

        /**
         * The SingleCharacterFilter class accepts a single specified character.
         */
        private static class SingleCharacterFilter implements CharacterFilter {
            private final char target;

            /**
             * Creates a SingleCharacterFilter instance with a specified character to accept.
             *
             * @param target the character to accept
             */
            SingleCharacterFilter(char target) {
                this.target = target;
            }

            /**
             * Checks if the given character is equal to the specified target character.
             *
             * @param c the character to check
             * @return true if the character is equal to the target character, false otherwise
             */
            @Override
            public boolean accept(char c) {
                return c == target;
            }
        }

    }
}
