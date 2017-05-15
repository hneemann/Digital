package de.neemann.digital.draw.library;

import java.util.Comparator;

import static java.lang.Character.isDigit;

/**
 * String comparator.
 * If the string begins with a digit, the numbers are taken to compare the two strings.
 * Used to ensure the 74xx components appear in the correct order instead of lexical order.
 * Created by hneemann on 15.05.17.
 */
public final class NumStringComparator implements Comparator<String> {

    private static final class InstanceHolder {
        private static final NumStringComparator INSTANCE = new NumStringComparator();
    }

    /**
     * Returns a comparator instance
     *
     * @return the singleton instance
     */
    public NumStringComparator getInstance() {
        return InstanceHolder.INSTANCE;
    }

    private NumStringComparator() {
    }

    @Override
    public int compare(String a, String b) {
        return compareStr(a, b);
    }

    /**
     * Compare two strings
     *
     * @param a a string
     * @param b a string
     * @return the comparison result
     */
    public static int compareStr(String a, String b) {
        NumString na = new NumString(a);
        NumString nb = new NumString(b);
        return na.compareTo(nb);
    }

    private static final class NumString implements Comparable<NumString> {
        private final int num;
        private final String str;
        private final boolean isNum;

        private NumString(String str) {
            str = str.trim();
            if (str.length() > 0 && isDigit(str.charAt(0))) {
                isNum = true;
                int n = 0;
                int i = 0;
                while (i < str.length() && isDigit(str.charAt(i))) {
                    n = n * 10 + (str.charAt(i) - '0');
                    i++;
                }
                this.str = str.substring(i);
                this.num = n;
            } else {
                this.str = str;
                num = 0;
                isNum = false;
            }
        }

        @Override
        public int compareTo(NumString other) {
            if (isNum && other.isNum) {
                if (num != other.num)
                    return num - other.num;
                else
                    return str.compareToIgnoreCase(other.str);
            } else
                return str.compareToIgnoreCase(other.str);
        }
    }

}
