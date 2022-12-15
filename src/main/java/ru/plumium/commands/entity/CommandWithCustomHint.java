package ru.plumium.commands.entity;

import java.util.ArrayList;

public class CommandWithCustomHint {
    private final String command;

    private final ArrayList<Hint> hints = new ArrayList<>();

    public CommandWithCustomHint(String commandName) {
        this.command = '/' + commandName;
    }

    public String getCommand() {
        return command;
    }

    public ArrayList<Hint> getHints() {
        return hints;
    }

    public void addHint(Hint hint) {
        hints.add(hint);
    }
}
