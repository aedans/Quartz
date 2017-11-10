package quartz.lang;

/**
 * The type of null
 */
public final class Null {
    private Null() {
        // Cannot construct Null
        throw new Error("Cannot construct Null");
    }

    /**
     * Returns the global Null instance
     */
    public static Null getInstance() {
        return null;
    }
}
