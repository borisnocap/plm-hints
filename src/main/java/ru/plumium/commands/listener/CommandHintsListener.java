package ru.plumium.commands.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.TabCompleteEvent;
import org.bukkit.plugin.Plugin;
import ru.plumium.commands.entity.CommandWithHint;
import ru.plumium.commands.entity.Hint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CommandHintsListener implements Listener {

    // Сюда добавляются и хранятся все команды с подсказками, которые зарегистрировали другие плагины.
    private final HashMap<String, CommandWithHint> commandsWithHint = new HashMap<>();

    public CommandHintsListener(Plugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    // Метод для добавления новых команд с подсказками.
    // Нужен, потому что хешмапа commandsWithHint приватная.
    public void registerNewCommandWithHint(CommandWithHint command) {
        commandsWithHint.put(command.getCommand(), command);
    }

    // Ивент вызывается, когда игрок ввел какую-то команду в чат (еще не отправил) и сервер пытается отправить
    // ему список предложенных продолжений команды. Например, игрок ввел "/plugin ", и сервер отправил ему "reload".
    // Таким образом, сервер показал игроку, что у команды "/plugin" может быть продолжение в виде аргумента "reload".
    @EventHandler()
    public void onTabCompleteEvent(final TabCompleteEvent event) {
        // Получаем введенную игроком комбинацию из команд и аргументов. То есть полную строку поля ввода сообщения в чате.
        String buffer = event.getBuffer();
        // Делим полученную строку по символам ' ' (пробела), чтобы выделить отдельные аргументы.
        String[] bufferArgs = buffer.split(" ");
        // Узнаем, является ли последним символом в буфере символ ' ' (пробела). Подсказки следующего аргумента должны
        // появляться только после ввода символа пробела.
        boolean lastBufferCharIsSpace = buffer.charAt(buffer.length() - 1) == ' ';
        // Если список команд с подсказками не содержит введенную пользователем команду, просто прекращаем обработку ивента.
        if (!commandsWithHint.containsKey(bufferArgs[0])) {
            return;
        }
        // Обработка ивента продолжилась, выходит, что введенная пользователем команда имеет кастомные подсказки.
        // Очищаем список предложенных аргументов, стандартные предложения нам не понадобятся вообще.
        event.getCompletions().clear();
        CommandWithHint command = commandsWithHint.get(bufferArgs[0]);
        // Если количество аругментов в буфере равно 1 (то есть введена только сама команда), то мы формируем
        // список аргументов "первого уровня" и отправляем их игроку.
        if (bufferArgs.length == 1) {
            ArrayList<String> text = new ArrayList<>();
            command.getHints().forEach(hint -> text.addAll(hint.getText()));
            event.getCompletions().addAll(text);
        } else {
            // Игрок ввел один или более аргументов, высчитываем их количество.
            int bufferArgumentsAmount = bufferArgs.length - 1;
            int i = 1;
            // Получаем список подсказок текущего уровня (в данный момент это подсказки первого уровня)
            ArrayList<Hint> lastHints = command.getHints();
            // Из полученного списка подсказок формируем список их аргументов.
            final HashMap<String, Hint> lastHintsText = new HashMap<>();
            for (Hint hint: lastHints) {
                hint.getText().forEach(text -> lastHintsText.put(text, hint));
            }
            // Перебираем аргументы с первого до последнего
            while (i <= bufferArgumentsAmount) {
                String currentArgument = bufferArgs[i];
                // Если аргумент полностью содержится в списоке аргументов текущего уровня.
                // Например, у команды есть 2 аргумента текущего уровня - "arg1" и 'arg2".
                // Это условие сработает, если игрок полностью ввел один из этих аргументов. Например "/example arg1".
                if (lastHintsText.containsKey(currentArgument)) {
                    // Тогда мы определяем, какой подсказке соответствует введенный аргумент.
                    Hint currentHint = lastHintsText.get(currentArgument);
                    // Если у этой подсказки есть дочерние подсказки.
                    if (currentHint.hasChildHints()) {
                        // Тогда мы переходим к следующему уровню подсказок, получаем их данные.
                        lastHints = lastHintsText.get(currentArgument).getChildHints();
                        lastHintsText.clear();
                        for (Hint hint: lastHints) {
                            hint.getText().forEach(text -> lastHintsText.put(text, hint));
                        }
                        // Если в данный момент перебирается самый последний аргумент и последним символом аргумента является пробел,
                        // тогда выводим игроку аргументы следующего уровня.
                        // Оператор break не требуется, потому что мы и так находимся в конце цикла.
                        if (i == bufferArgumentsAmount && lastBufferCharIsSpace) {
                            event.getCompletions().addAll(lastHintsText.keySet());
                        }
                        // Если у этой подсказки нет дочерних подсказок, прекращаем перебор аргументов, а игрок
                        // получает пустой список доступных аргументов.
                    } else {
                        break;
                    }
                    // Если введенный игроком аргумент введен не полностью.
                } else {
                    // Формируем список аргументов, начинающихся с символов, введенных игроком.
                    List<String> hintsStartsWithBufferArgument = new ArrayList<>();
                    for (Hint currentHint : lastHints) {
                        List<String> currentHintText = currentHint.getText();
                        hintsStartsWithBufferArgument.addAll(currentHintText.stream().filter(s -> s.startsWith(currentArgument)).toList());
                    }
                    // Если ни один аргумент не начинается с этой комбинации символов, то прекращаем перебор аргументов
                    // и отправляем игроку пустой список аргументов.
                    if (hintsStartsWithBufferArgument.size() == 0) {
                        break;
                    } else {
                        // Если найден хотя бы один подходящий аргумент, проверяем, что сейчас проверяется последний
                        // введенный аргумент, и что последний введенный символ не является пробелом.
                        // Если проверка прошла успешно, тогда отправляем игроку список аргументов, которые начинаются
                        // с введенных игроком символов.
                        if (i == bufferArgumentsAmount && !lastBufferCharIsSpace) {
                            event.getCompletions().addAll(hintsStartsWithBufferArgument);
                            // Если мы обрабатываем не последний аргумент, или в конце буфера обнаружен пробел, тогда
                            // прекращаем перебор аргументов и отправляем игроку пустой список.
                        } else {
                            break;
                        }
                    }
                }
                i++;
            }
        }
    }
}
