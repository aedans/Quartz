package quartz.lang;

/**
 * Utility class for running Quartz programs
 */
public final class Runner {
    /**
     * Runs a Quartz program's main function
     *
     * @param main The main function to run
     * @param args The arguments from standard in
     */
    public static void run(Function<Unit, Unit> main, String[] args) {
        main.invoke(Unit.getInstance());
    }
}
