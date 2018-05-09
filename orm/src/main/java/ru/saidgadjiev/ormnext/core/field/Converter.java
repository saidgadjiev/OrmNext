package ru.saidgadjiev.ormnext.core.field;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Converter {

    Class<? extends ru.saidgadjiev.ormnext.core.field.persister.Converter> value();

    String[] args() default {};

}
