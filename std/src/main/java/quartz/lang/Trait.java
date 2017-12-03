package quartz.lang;

import java.lang.annotation.*;

/**
 * Marker interface for Quartz traits.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Trait {
}
