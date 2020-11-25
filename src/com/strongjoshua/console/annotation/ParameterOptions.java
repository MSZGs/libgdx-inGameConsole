package com.strongjoshua.console.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
public @interface ParameterOptions {
    ParameterOption[] value();
}
