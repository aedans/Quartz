package quartz.lang;

import java.lang.annotation.*;

/**
 * Marker interface for Quartz values.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Value {
}
