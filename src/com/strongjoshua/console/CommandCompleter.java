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

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.ObjectSet.ObjectSetIterator;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Method;
import com.strongjoshua.console.annotation.ParameterOption;
import com.strongjoshua.console.annotation.ParameterOptions;

import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommandCompleter {
    private final ObjectSet<String> possibleCommands;
    private ObjectSetIterator<String> iterator;
    private String setString;

    public CommandCompleter() {
        possibleCommands = new ObjectSet<>();
        setString = "";
    }

    public void completePrefix(Iterable<ICommandContainer> commandContainers, String start) {
        for (ICommandContainer container : commandContainers) {
            if (container.getCommandPrefix().toLowerCase().startsWith(start.toLowerCase())) {
                possibleCommands.add(container.getCommandPrefix());
            }
        }
    }

    public void completeCommand(ICommandContainer commandContainer, String start, String pref) {
        Method[] methods = ConsoleUtils.getAllMethods(commandContainer).toArray(Method[]::new);
        for (Method m : methods) {
            if (m.getName().startsWith(start)) {
                possibleCommands.add(pref + " " + m.getName());
            }
        }
    }

    public void completeParameter(ICommandContainer commandContainer, String methodName, int paramIndex, String start, String pref) {
        if (commandContainer instanceof IConsoleAutoCompleterSupport) {
            Method[] methods = ConsoleUtils.getAllMethods(commandContainer, methodName).toArray(Method[]::new);
            for (Method method : methods) {
                ArrayList<ParameterOption> options = new ArrayList<>();

                if (method.isAnnotationPresent(ParameterOptions.class)) {
                    options.addAll(Arrays.asList(method
                            .getDeclaredAnnotation(ParameterOptions.class)
                            .getAnnotation(ParameterOptions.class)
                            .value()));
                }
                if (method.isAnnotationPresent(ParameterOption.class)) {
                    options.add(method
                            .getDeclaredAnnotation(ParameterOption.class)
                            .getAnnotation(ParameterOption.class));
                }

                options.stream()
                        .filter(x -> x.index() == paramIndex)
                        .findFirst()
                        .ifPresent(x -> {
                            String[] a = ((IConsoleAutoCompleterSupport) commandContainer).getAutocompleteOptions(x.id());
                            for (String k : a) {
                                if (k.startsWith(start)) {
                                    possibleCommands.add(pref + " " + k);
                                }
                            }
                        });

            }
        }
    }

    public void set(Map<String, ICommandContainer> ces, String s) {
        reset();
        List<String> parts = Arrays.stream(s.split(" ")).collect(Collectors.toList());
        if (s.endsWith(" ")) {
            parts.add("");
        }

        setString = s;

        String prefix = parts.stream()
                .limit(parts.size() - 1)
                .collect(Collectors.joining(" "));

        if (parts.size() == 1) { //Prefix
            completePrefix(ces.values(), parts.get(0));
        } else if (parts.size() == 2) { //Command
            String commandContainerPrefix = parts.get(0).toLowerCase();
            if (ces.containsKey(commandContainerPrefix)) {
                ICommandContainer container = ces.get(commandContainerPrefix);

                completeCommand(container, parts.get(1), prefix);
            }
        } else if (parts.size() > 2) { //Params
            String commandContainerPrefix = parts.get(0).toLowerCase();
            if (ces.containsKey(commandContainerPrefix)) {
                ICommandContainer container = ces.get(commandContainerPrefix);

                completeParameter(container,
                        parts.get(1),
                        parts.size() - 3,
                        parts.get(parts.size() - 1),
                        prefix);
            }
        }
        iterator = new ObjectSetIterator<>(possibleCommands);
    }

    public void reset() {
        possibleCommands.clear();
        setString = "";
        iterator = null;
    }

    public boolean isNew() {
        return possibleCommands.size == 0;
    }

    public boolean wasSetWith(String s) {
        return setString.equalsIgnoreCase(s);
    }

    public String next() {
        if (!iterator.hasNext) {
            iterator.reset();
            return setString;
        }
        return iterator.next();
    }
}
