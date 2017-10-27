package quartz.lang;

/**
 * @author Aedan Smith
 */

public final class Runner {
    public static void run(Function<Unit, Unit> main, String[] args) {
        main.invoke(Unit.getInstance());
    }
}
