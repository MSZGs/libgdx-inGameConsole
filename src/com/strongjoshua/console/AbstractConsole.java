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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.OrderedSet;
import com.badlogic.gdx.utils.reflect.Annotation;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Method;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.strongjoshua.console.annotation.ConsoleCommand;
import com.strongjoshua.console.annotation.ConsoleDoc;
import com.sun.org.apache.xpath.internal.operations.Bool;

import java.util.*;
import java.util.stream.Stream;

/**
 * @author Eric
 */
public abstract class AbstractConsole implements Console, Disposable {
    protected final Log log;
    protected boolean logToSystem;

    protected boolean disabled;

    protected boolean executeHiddenCommands = true;
    protected boolean displayHiddenCommands = false;
    protected boolean consoleTrace = false;

    protected HashMap<String, ICommandContainer> _commandContainers;

    public AbstractConsole() {
        log = new Log();
        _commandContainers = new HashMap<>();
        addCommandContainer(new HelpCommands(this));
    }

    @Override
    public void setLoggingToSystem(Boolean log) {
        this.logToSystem = log;
    }

    @Override
    public void log(String msg, LogLevel level) {
        log.addEntry(msg, level);

        if (logToSystem) {
            switch (level) {
                case ERROR:
                    System.err.println("> " + msg);
                    break;
                default:
                    System.out.println("> " + msg);
                    break;
            }
        }
    }

    @Override
    public void log(String msg) {
        this.log(msg, LogLevel.DEFAULT);
    }

    @Override
    public void log(Throwable exception, LogLevel level) {
        this.log(ConsoleUtils.exceptionToString(exception), level);
    }

    @Override
    public void log(Throwable exception) {
        this.log(exception, LogLevel.ERROR);
    }

    @Override
    public void printLogToFile(String file) {
        this.printLogToFile(Gdx.files.local(file));
    }

    @Override
    public void printLogToFile(FileHandle fh) {
        if (log.printToFile(fh)) {
            log("Successfully wrote logs to file.", LogLevel.SUCCESS);
        } else {
            log("Unable to write logs to file.", LogLevel.ERROR);
        }
    }

    @Override
    public boolean isDisabled() {
        return disabled;
    }

    @Override
    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    @Override
    public void addCommandContainer(ICommandContainer commandExecutor) {
        _commandContainers.put(commandExecutor.getCommandPrefix().trim().toLowerCase(), commandExecutor);
    }

    @Override
    public void removeCommandContainer(ICommandContainer commandExecutor) {
        _commandContainers.remove(commandExecutor.getCommandPrefix());
    }

    public String[] getCommandContainerPrefixes() {
        return _commandContainers.keySet().toArray(new String[0]);
    }

    public Boolean isHasCommandContainer() {
        return !_commandContainers.isEmpty();
    }

    @Override
    public void execCommand(String commandString) {
        if (disabled)
            return;

        log(commandString, LogLevel.COMMAND);

        String[] commandStringParts = commandString.split(" ");


        String commandContainerName = commandStringParts[0].toLowerCase().trim();
        String command = null;
        if (commandStringParts.length > 1) {
            command = commandStringParts[1].toLowerCase().trim();
        }

        String[] commandArgs = Arrays.stream(commandStringParts).skip(2).toArray(String[]::new);

        if (_commandContainers.containsKey(commandContainerName)) {
            ICommandContainer container = _commandContainers.get(commandContainerName);
            if (command != null) {
                execCommand(container, command, commandArgs);
            } else {
                execCommand(container);
            }
        } else {
            log("No such method found.", LogLevel.ERROR);
        }
    }

    private void execCommand(ICommandContainer exec) {
        exec.defaultConsoleCommand();
    }

    private void execCommand(ICommandContainer exec, String command, String[] stringArgs) {

        Method[] availableMethods = ConsoleUtils.getAllMethods(exec, command)
                .filter(x -> ConsoleUtils.canExecuteCommand(this, x))
                .toArray(Method[]::new);

        if (availableMethods.length <= 0) {
            log("No such method found.", LogLevel.ERROR);
            return;
        }

        for (Method method : availableMethods) {
            try {
                Object[] args = null;
                try {
                    args = ConsoleUtils.parseCommandArgs(method, stringArgs);
                } catch (Exception e) {
                    continue;
                }

                method.setAccessible(true);
                method.invoke(exec, args);
                return;
            } catch (ReflectionException e) {
                String msg = e.getMessage();
                if (msg == null || msg.length() <= 0) {
                    msg = "Unknown Error";
                    e.printStackTrace();
                }
                log(msg, LogLevel.ERROR);
                if (consoleTrace) {
                    log(e, LogLevel.ERROR);
                }
                return;
            }

        }
        log("Bad parameters. Check your code.", LogLevel.ERROR);
    }

    @Override
    public void printCommands() {
        for (ICommandContainer container : _commandContainers.values()) {
            StringBuilder s = new StringBuilder()
                    .append(container.getCommandPrefix())
                    .append(":\n");


            boolean display = false;
            Method[] methods = ConsoleUtils.getAllMethods(container).toArray(Method[]::new);
            for (Method method : methods) {
                if (method.isPublic() && ConsoleUtils.canDisplayCommand(this, method)) {
                    s.append("       ")
                            .append(" ")
                            .append(method.getName());
                    ConsoleUtils.methodParamsToString(method, s);
                    s.append("\n");
                    display = true;
                }
            }
            if (display) {
                log(s.toString());
            }
        }
    }

    @Override
    public void printHelp(String commandContainerName) {
        String normName = commandContainerName.trim().toLowerCase();
        if (_commandContainers.containsKey(normName)) {
            ICommandContainer container = _commandContainers.get(normName);
            StringBuilder sb = new StringBuilder()
                    .append("Help for ")
                    .append(container.getCommandPrefix())
                    .append(":\n\n");

            Method[] methods = ConsoleUtils.getAllMethods(container).toArray(Method[]::new);
            for (Method method : methods) {
                Annotation annotation = method.getDeclaredAnnotation(ConsoleDoc.class);
                sb.append("-").append(method.getName());
                ConsoleUtils.methodParamsToString(method, sb)
                        .append(":\n");


                if (annotation != null) {
                    ConsoleDoc doc = annotation.getAnnotation(ConsoleDoc.class);
                    sb.append(doc.description()).append("\n\n");

                    Class<?>[] params = method.getParameterTypes();
                    for (int i = 0; i < params.length; i++) {
                        for (int j = 0; j < method.getName().length() + 2; j++)
                            // using spaces this way works with monotype fonts
                            sb.append(" ");
                        sb.append(params[i].getSimpleName()).append(": ");
                        if (i < doc.paramDescriptions().length)
                            sb.append(doc.paramDescriptions()[i]);
                    }
                }
            }
            log(sb.toString());
        } else {
            log("Bad parameters. Check your code.", LogLevel.ERROR);
        }
    }

    @Override
    public void setExecuteHiddenCommands(boolean enabled) {
        executeHiddenCommands = enabled;
    }

    @Override
    public boolean isExecuteHiddenCommandsEnabled() {
        return executeHiddenCommands;
    }

    @Override
    public void setDisplayHiddenCommands(boolean enabled) {
        displayHiddenCommands = enabled;
    }

    @Override
    public boolean isDisplayHiddenCommandsEnabled() {
        return displayHiddenCommands;
    }

    @Override
    public void setConsoleStackTrace(boolean enabled) {
        this.consoleTrace = enabled;
    }

    @Override
    public void setMaxEntries(int numEntries) {
    }

    @Override
    public void clear() {
    }

    @Override
    public void setSize(int width, int height) {
    }

    @Override
    public void setSizePercent(float wPct, float hPct) {
    }

    @Override
    public void setPosition(int x, int y) {
    }

    @Override
    public void setPositionPercent(float xPosPct, float yPosPct) {
    }

    @Override
    public void resetInputProcessing() {
    }

    @Override
    public InputProcessor getInputProcessor() {
        return null;
    }

    @Override
    public void draw() {
    }

    @Override
    public void refresh() {
    }

    @Override
    public void refresh(boolean retain) {
    }

    @Override
    public int getDisplayKeyID() {
        return 0;
    }

    @Override
    public void setDisplayKeyID(int code) {
    }

    @Override
    public boolean hitsConsole(float screenX, float screenY) {
        return false;
    }

    @Override
    public void dispose() {
    }

    @Override
    public boolean isVisible() {
        return false;
    }

    @Override
    public void setVisible(boolean visible) {
    }

    @Override
    public void select() {
    }

    @Override
    public void deselect() {
    }

    @Override
    public void setTitle(String title) {
    }

    @Override
    public void setHoverAlpha(float alpha) {
    }

    @Override
    public void setNoHoverAlpha(float alpha) {
    }

    @Override
    public void setHoverColor(Color color) {
    }

    @Override
    public void setNoHoverColor(Color color) {
    }

    @Override
    public void enableSubmitButton(boolean enable) {
    }

    @Override
    public void setSubmitText(String text) {
    }

    @Override
    public Window getWindow() {
        return null;
    }
}
