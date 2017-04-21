package de.his.core.tools.cs.sys.quality.eclipsemacker.util;

import java.io.IOException;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

/**
 * Helper class for logging messages to a console.
 * 
 * @author keunecke, tneumann
 */
public class ConsoleLogger {

    /** project in which context we are logging */
    private final String targetProjectName;

    private final String consoleName;

    private boolean loggingEnabled = false;

    /**
     * Create a new ConsoleLogger.
     * 
     * @param targetProjectName
     * @param consoleName
     * @param loggingEnabled;
     */
    public ConsoleLogger(String targetProjectName, String consoleName, boolean loggingEnabled) {
        this.targetProjectName = targetProjectName;
        this.consoleName = consoleName;
        this.loggingEnabled = loggingEnabled;
    }

    /**
     * Log a message to the console
     * 
     * @param message
     */
    public void logToConsole(String message) {
        if (loggingEnabled) {
            MessageConsole console = findConsole(consoleName);
            MessageConsoleStream newMessageStream = console.newMessageStream();
            try {
                newMessageStream.write("[" + targetProjectName + "] " + message + "\n");
                newMessageStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    newMessageStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private MessageConsole findConsole(String name) {
        ConsolePlugin plugin = ConsolePlugin.getDefault();
        IConsoleManager conMan = plugin.getConsoleManager();
        IConsole[] existing = conMan.getConsoles();
        for (int i = 0; i < existing.length; i++) {
            if (name.equals(existing[i].getName())) {
            	return (MessageConsole) existing[i];
            }
        }
        //no console found, so create a new one
        MessageConsole myConsole = new MessageConsole(name, null);
        conMan.addConsoles(new IConsole[] { myConsole });
        return myConsole;
    }
}
