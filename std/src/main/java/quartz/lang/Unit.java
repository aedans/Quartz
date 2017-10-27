package quartz.lang;

/**
 * @author Aedan Smith
 */

public final class Unit {
    private Unit() {
    }

    private static final Unit INSTANCE = new Unit();

    public static Unit getInstance() {
        return INSTANCE;
    }

    @Override
    public String toString() {
        return "Unit";
    }
}
