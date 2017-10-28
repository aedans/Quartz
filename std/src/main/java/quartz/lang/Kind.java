package quartz.lang;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for representing higher kinded type parameters
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE_PARAMETER)
public @interface Kind {
    /**
     * The kind of the type parameter in the form
     * kind ::= '*'
     *        | '(' kind ')'
     *        | kind '->' kind
     */
    String value();
}
