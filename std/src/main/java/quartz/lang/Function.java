package quartz.lang;

/**
 * @author Aedan Smith
 */

public interface Function<A, B> {
    B invoke(A a);
}
