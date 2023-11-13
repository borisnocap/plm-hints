package ru.plm.hints;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public record HintCompleter(ArrayList<Hint> hints) implements TabCompleter {

    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        int argumentsAmount = strings.length;
        int lastArgumentIndex = argumentsAmount - 1;
        boolean lastArgumentIsEmpty = strings[lastArgumentIndex].isEmpty();
        ArrayList<Hint> lastHints = hints;
        HashMap<String, Hint> lastHintsText = new HashMap<>();
        for (Hint hint : lastHints) {
            hint.getText(commandSender).forEach(text -> lastHintsText.put(text, hint));
        }
        int i = 0;
        while (i < argumentsAmount) {
            String currentArgument = strings[i];
            if (i == lastArgumentIndex && lastArgumentIsEmpty) {
                return lastHintsText.keySet().stream().toList();
            } else if (lastHintsText.containsKey(currentArgument)) {
                Hint currentHint = lastHintsText.get(currentArgument);
                if (currentHint.hasChildHints()) {
                    lastHints = currentHint.getChildHints();
                    lastHintsText.clear();
                    for (Hint hint : lastHints) {
                        hint.getText(commandSender).forEach(text -> lastHintsText.put(text, hint));
                    }
                } else {
                    return List.of();
                }
            } else {
                ArrayList<String> hintsStartsWith = new ArrayList<>();
                for (Hint currentHint : lastHints) {
                    List<String> currentHintText = currentHint.getText(commandSender);
                    hintsStartsWith.addAll(currentHintText.stream().filter(text -> text.toLowerCase().startsWith(currentArgument.toLowerCase())).toList());
                }
                if (i != lastArgumentIndex) {
                    return List.of();
                } else {
                    return hintsStartsWith;
                }
            }
            i++;
        }
        return List.of();
    }
}
