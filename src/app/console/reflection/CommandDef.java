package app.console.reflection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Alexey on 18.07.2016.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.METHOD)
public @interface CommandDef {
    /**
     * This provides description when generating docs.
     */
    public String desc() default "";
    /**
     * This provides params when generating docs.
     */
    public String[] params();
}