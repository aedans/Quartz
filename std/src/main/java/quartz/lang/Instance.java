package quartz.lang;

import java.lang.annotation.*;

/**
 * Marker annotation for Quartz instances.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Instance(Class.class)
public @interface Instance {
    Class<?> value();
}
