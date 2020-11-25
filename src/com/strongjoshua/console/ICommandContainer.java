package com.strongjoshua.console;

public interface ICommandContainer {
    String getCommandPrefix();

    default void defaultConsoleCommand() {

    }
}
