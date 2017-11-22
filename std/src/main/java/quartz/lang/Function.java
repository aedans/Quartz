package quartz.lang;

/**
 * Interface representing all Quartz functions.
 *
 * @param <A> The input type of the function.
 * @param <B> The output type of the function.
 */
public interface Function<A, B> {
    B invoke(A a);
}
