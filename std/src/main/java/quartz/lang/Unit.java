package quartz.lang;

/**
 * The type of "()"
 */
public final class Unit {
    private static final Unit INSTANCE = new Unit();

    public static Unit getInstance() {
        return INSTANCE;
    }

    private Unit() {
        // Do not construct unit
    }
}
