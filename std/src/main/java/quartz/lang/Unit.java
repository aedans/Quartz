package quartz.lang;

/**
 * The type of "()".
 */
public final class Unit extends Any {
    public static final Unit INSTANCE = new Unit();

    private Unit() {
        // Do not construct unit
    }
}
