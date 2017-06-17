package de.neemann.digital.core;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * A exception which has a set of files as an origin.
 * Created by hneemann on 16.06.17.
 */
public class ExceptionWithOrigin extends Exception {
    private Set<File> origin;


    /**
     * Returns the file or the files that caused the given exception.
     * If no origin is found null is returned.
     *
     * @param e the exception
     * @return the origin or null;
     */
    public static String getOrigin(Throwable e) {
        while (e != null) {
            if (e instanceof ExceptionWithOrigin) {
                String orig = ((ExceptionWithOrigin) e).getOriginStr();
                if (orig != null)
                    return orig;
            }
            e = e.getCause();
        }
        return null;
    }

    /**
     * Creates a new exception
     *
     * @param message message
     */
    public ExceptionWithOrigin(String message) {
        super(message);
    }

    /**
     * Creates a new exception
     *
     * @param message message
     * @param cause   the cause
     */
    public ExceptionWithOrigin(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new exception
     *
     * @param cause the cause
     */
    public ExceptionWithOrigin(Throwable cause) {
        super(cause);
    }

    /**
     * @return the origin of the error
     */
    public Set<File> getOrigin() {
        return origin;
    }

    /**
     * @return the origin of the error as a string
     */
    private String getOriginStr() {
        Set<File> orig = getOrigin();
        if (orig == null || orig.isEmpty())
            return null;

        StringBuilder sb = new StringBuilder();
        for (File o : orig) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(o.getName());
        }
        return sb.toString();
    }

    /**
     * Sets the origin of an error
     *
     * @param origin the file which had caused the exception
     */
    public void setOrigin(File origin) {
        if (this.origin == null && origin != null) {
            this.origin = new HashSet<>();
            this.origin.add(origin);
        }
    }

}
