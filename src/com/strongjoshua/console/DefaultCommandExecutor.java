package com.strongjoshua.console;

import com.badlogic.gdx.Gdx;
import com.strongjoshua.console.annotation.ConsoleDoc;

public class DefaultCommandExecutor extends CommandExecutor {
    /**
     * Prints the log to a local file.
     *
     * @param path The relative path of the local file to print to.
     */
    public final void printLog (String path) {
        console.printLogToFile(path);
    }

    /**
     * Closes the application completely.
     */
    @ConsoleDoc(description = "Exits the application.") public final void exitApp () {
        Gdx.app.exit();
    }

    /**
     * Shows all available methods, and their parameter types, in the console.
     */
    @ConsoleDoc(description = "Shows all available methods.") public final void help () {
        console.printCommands();
    }

    /**
     * Prints out ConsoleDoc for the given command, if it exists.
     *
     * @param command The command to get help for.
     */
    @ConsoleDoc(description = "Prints console docs for the given command.") public final void help (String command) {
        console.printHelp(command);
    }

    /**
     * Deselects the text field in the console. Gives keyboard control back to the application.
     */
    @ConsoleDoc(description = "Deselects the console text field. Click to " + "re-select.") public final void logView () {
        console.deselect();
    }
}
