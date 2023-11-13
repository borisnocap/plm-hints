package ru.plm.hints;

import org.apache.commons.lang3.math.NumberUtils;
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
        // Количество введенных игроком аргументов команды
        int argumentsAmount = strings.length;
        // Индекс последнего аргумента в массиве аргументов
        int lastArgumentIndex = argumentsAmount - 1;
        // Последний аргумент может быть пустым, если игрок еще не ввел ни одного аргумента.
        boolean lastArgumentIsEmpty = strings[lastArgumentIndex].isEmpty();
        /*
        Список подсказок, которых скрипт обрабатывает в данный момент.
        При инициализации сюда помещаются подсказки первого уровня.
        В процессе обработки введенных игроком аргументов в эту могут быть помещены дочерние подсказки */
        ArrayList<Hint> currentLevelHints = hints;
        // Тип подсказок текущего уровня (все подсказки одного уровня имеют один тип).
        String currentLevelType = "TEXT";
        /*
        Мапа, которая хранит все возможные значения аргументов и подсказок, к которым они относятся.
        Например:
        remove, Hint("TEXT", "remove")
        add, Hint ("TEXT", "add")
        Player1, Hint("PLAYER", "<игрок>")
        Player2, Hint("PLAYER", "<игрок>") */
        HashMap<String, Hint> currentLevelHintsText = new HashMap<>();
        for (Hint hint : currentLevelHints) {
            hint.getText(commandSender).forEach(text -> currentLevelHintsText.put(text, hint));
            currentLevelType = hint.getType();
        }
        int i = 0;
        // Нужно пройтись по всем введенным аргументам, начиная с самого первого
        while (i < argumentsAmount) {
            String currentArgument = strings[i];
            /*
            Если сейчас обрабатывается последний аргумент, и последний аргумент пустой (игрок не ввел ни одного
            аргумента), тогда выводим игроку все начальные подсказки */
            if (i == lastArgumentIndex && lastArgumentIsEmpty)  return currentLevelHintsText.keySet().stream().toList();
            /*
            Если подсказки текущего уровня имеют тип NUMBER + введенный аргумент состоит только из цифр + подсказка
            имеет дочерний уровень подсказок */
            if (currentLevelType.equals(HintType.NUMBER.toString()) && NumberUtils.isDigits(currentArgument) && currentLevelHints.get(0).hasChildHints()) {
                /*
                На одном уровне может находиться только одна подсказка типа NUMBER, поэтому мы можем получить ее
                с помощью get(0) */
                currentLevelHints = currentLevelHints.get(0).getChildHints();
                currentLevelHintsText.clear();
                for (Hint hint : currentLevelHints) {
                    hint.getText(commandSender).forEach(text -> currentLevelHintsText.put(text, hint));
                    currentLevelType = hint.getType();
                }
            }
            // Если среди аргументов текущего уровня есть введенный аргумент (и он полностью введен)
            else if (currentLevelHintsText.containsKey(currentArgument)) {
                // Получаем подсказку, к которой относится аргумент
                Hint currentHint = currentLevelHintsText.get(currentArgument);
                // Если у подсказки нет дочерних подсказок, тогда выводим игроку пустой список предложений
                if (!currentHint.hasChildHints()) return List.of();
                /*
                Если же подсказка имеет дочерние подсказки, тогда обновляем список последних подсказок и мапу
                аргументов, помещая туда дочерние подсказки */
                currentLevelHints = currentHint.getChildHints();
                currentLevelHintsText.clear();
                for (Hint hint : currentLevelHints) {
                    hint.getText(commandSender).forEach(text -> currentLevelHintsText.put(text, hint));
                    currentLevelType = hint.getType();
                }
            }
            // Если среди аргументов текущего уровня нет введенного аргумента (возможно он введен частично)
            else {
                /*
                Если сейчас обрабатывается не последний введенный аргумент, возвращаем пустой список предложений,
                потому что не последний аргумент не может быть введен не полностью (у него должны были быть найдены
                совпадения на предыдущих этапах) */
                if (i != lastArgumentIndex) return List.of();
                /*
                Если же сейчас обрабатывается последний введенный аргумент, возвращаем список предложений, которые
                начинаются с таким же символов, что и введенный аргумент.
                Формируем список предложений, которые начинаются так же, как введенный аргумент.
                Например, допустимыми аргументами текущего уровня являются "add", "remove" и "release".
                Игроком введен аргумент "rem".
                Все допустимые аргументы будут проверены на то, что они начинаются с "rem" и подходящие аргументы
                будут добавлены в список предложений */
                ArrayList<String> hintsStartsWith = new ArrayList<>();
                for (Hint currentHint : currentLevelHints) {
                    List<String> currentHintText = currentHint.getText(commandSender);
                    hintsStartsWith.addAll(currentHintText.stream().filter(text -> text.toLowerCase().startsWith(currentArgument.toLowerCase())).toList());
                }
                return hintsStartsWith;
            }
            // После обработки аргумента, переходим к следующему
            i++;
        }
        /*
        Если игрок ввел аргумент, у этого аргумента имеется дочерний уровень подсказок, но игрок еще не ввел
        пробел после введенного аргумента, тогда возвращаем ему пустой список предложений */
        return List.of();
    }
}
