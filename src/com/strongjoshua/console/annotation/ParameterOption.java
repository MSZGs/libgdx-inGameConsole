package com.strongjoshua.console.annotation;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Repeatable(ParameterOptions.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface ParameterOption {
    int index();

    int id();
}
