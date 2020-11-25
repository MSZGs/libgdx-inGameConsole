/**
 * Copyright 2018 StrongJoshua (strongjoshua@hotmail.com)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */

package com.strongjoshua.console;

import com.badlogic.gdx.utils.reflect.Annotation;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Method;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.strongjoshua.console.annotation.ConsoleCommand;
import com.strongjoshua.console.annotation.ConsoleDoc;
import com.strongjoshua.console.annotation.HiddenCommand;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * @author Eric
 */
public final class ConsoleUtils {

    public static Stream<Method> getAllMethods(ICommandContainer exec, String command) {
        return getAllMethods(exec)
                .filter(x -> (x.getName().equalsIgnoreCase(command)) ||
                        x.getDeclaredAnnotation(ConsoleCommand.class)
                                .getAnnotation(ConsoleCommand.class)
                                .commandName()
                                .equalsIgnoreCase(command));
    }

    public static Stream<Method> getAllMethods(ICommandContainer exec) {
        return Arrays
                .stream(ClassReflection.getMethods(exec.getClass()))
                .filter(x -> x.isAnnotationPresent(ConsoleCommand.class));
    }

    public static StringBuilder methodParamsToString(Method method, StringBuilder builder) {
        builder.append("(");
        Class<?>[] params = method.getParameterTypes();
        if (params.length > 0) {
            for (int i = 0; i < params.length; i++) {
                ConsoleCommand annotation = method
                        .getDeclaredAnnotation(ConsoleCommand.class)
                        .getAnnotation(ConsoleCommand.class);
                if (annotation.parameterNames().length > i) {
                    builder.append(annotation.parameterNames()[i]);
                    builder.append(":");
                }

                builder.append(params[i].getSimpleName());

                if (i < params.length - 1) {
                    builder.append(", ");
                }
            }
        } else {
            builder.append("void");
        }
        builder.append(")");
        return builder;
    }

    public static Object[] parseCommandArgs(Method method, String[] stringArgs) throws Exception {
        Class<?>[] params = method.getParameterTypes();
        Object[] args = new Object[stringArgs.length];
        if (params.length != args.length) throw new Exception();
        for (int j = 0; j < params.length; j++) {
            Class<?> param = params[j];
            final String value = stringArgs[j];

            if (param.equals(String.class)) {
                args[j] = value;
            } else if (param.equals(Boolean.class) || param.equals(boolean.class)) {
                args[j] = Boolean.parseBoolean(value);
            } else if (param.equals(Byte.class) || param.equals(byte.class)) {
                args[j] = Byte.parseByte(value);
            } else if (param.equals(Short.class) || param.equals(short.class)) {
                args[j] = Short.parseShort(value);
            } else if (param.equals(Integer.class) || param.equals(int.class)) {
                args[j] = Integer.parseInt(value);
            } else if (param.equals(Long.class) || param.equals(long.class)) {
                args[j] = Long.parseLong(value);
            } else if (param.equals(Float.class) || param.equals(float.class)) {
                args[j] = Float.parseFloat(value);
            } else if (param.equals(Double.class) || param.equals(double.class)) {
                args[j] = Double.parseDouble(value);
            }
        }
        return args;
    }

    ;

    public static boolean canExecuteCommand(Console console, Method method) {
        return console.isExecuteHiddenCommandsEnabled() || !method.isAnnotationPresent(HiddenCommand.class);
    }

    public static boolean canDisplayCommand(Console console, Method method) {
        return console.isDisplayHiddenCommandsEnabled() || !method.isAnnotationPresent(HiddenCommand.class);
    }

    public static String exceptionToString(final Throwable throwable) {
        StringBuilder result = new StringBuilder();
        Throwable cause = throwable;

        while (cause != null) {
            if (result.length() == 0) {
                result.append("\nException in thread \"").append(Thread.currentThread().getName()).append("\" ");
            } else {
                result.append("\nCaused by: ");
            }
            result.append(cause.getClass().getCanonicalName()).append(": ").append(cause.getMessage());

            for (final StackTraceElement traceElement : cause.getStackTrace()) {
                result.append("\n\tat ").append(traceElement.toString());
            }
            cause = cause.getCause();
        }
        return result.toString();
    }
}
