package com.gametac.bdb;

import java.util.StringTokenizer;
import java.util.Vector;

public enum Command {
    PRINT,
    UPDATE,
    QUIT,
    NONE;

    Vector<String> commandArguments = new Vector<String>();
    
    static Command getCommand(String line) throws InvalidCommandException {
        StringTokenizer tokenizer = new StringTokenizer(line);
        
        if (!tokenizer.hasMoreTokens()) {
            return NONE;
        }
        
        String commandString = tokenizer.nextToken();
        Command command = null;

        for (Command c : Command.values()) {
            if (c.name().equalsIgnoreCase(commandString)) {
                if (!tokenizer.hasMoreTokens()) {
                    return c;
                } else {
                	command = c;
                }
            }
        }
        
        if (command == null) {
        	throw new InvalidCommandException("The command line: " + line + ", didn't reach any valid command");
        }

        while (tokenizer.hasMoreElements()) {
        	String token = tokenizer.nextToken();
        	command.commandArguments.add(token);
        }

        return command;
    }

    @SuppressWarnings("serial")
    static class InvalidCommandException extends Exception {
        InvalidCommandException(String error) {
            super(error);
        }
    }
}
