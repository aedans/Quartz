package quartz.lang;

/**
 * Class representing the type of "()"
 */
public final class Unit {
    private Unit() {
        // Do not construct Unit
    }

    private static final Unit INSTANCE = new Unit();

    /**
     * Returns the global Unit instance
     */
    public static Unit getInstance() {
        return INSTANCE;
    }

    @Override
    public String toString() {
        return "Unit";
    }
}
