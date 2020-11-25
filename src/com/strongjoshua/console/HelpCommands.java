package com.strongjoshua.console;

import com.strongjoshua.console.annotation.ConsoleCommand;
import com.strongjoshua.console.annotation.ConsoleDoc;
import com.strongjoshua.console.annotation.ParameterOption;

public class HelpCommands implements ICommandContainer, IConsoleAutoCompleterSupport {
    private final AbstractConsole _console;

    public HelpCommands(AbstractConsole console) {
        _console = console;
    }

    @Override
    public String getCommandPrefix() {
        return "help";
    }

    @Override
    public String[] getAutocompleteOptions(int parameterID) {
        if (parameterID == 1) {
            return _console.getCommandContainerPrefixes();
        }
        return new String[0];
    }

    @Override
    public void defaultConsoleCommand() {
        _console.printCommands();
    }

    @ConsoleCommand
    @ConsoleDoc(description = "Shows all available methods.")
    public void print() {
        _console.printCommands();
    }

    @ParameterOption(index = 0, id = 1)
    @ParameterOption(index = 0, id = 1)
    @ConsoleCommand(parameterNames = "methodName")
    @ConsoleDoc(description = "Prints console docs for the given command.", paramDescriptions = {"given command"})
    public void print(String method) {
        _console.printHelp(method);
    }
}
