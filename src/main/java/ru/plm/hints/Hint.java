package ru.plm.hints;

import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Hint {

    protected final String type;
    protected final String text;
    protected final ArrayList<Hint> childHints = new ArrayList<>();

    public Hint(String type, String text) {
        this.type = type;
        this.text = text;
    }

    public String getType() {
        return type;
    }

    public ArrayList<Hint> getChildHints() {
        return childHints;
    }

    public boolean hasChildHints() {
        return !childHints.isEmpty();
    }

    public Hint addChildHint(Hint childHint) {
        childHints.add(childHint);
        return this;
    }

    /**
     * Метод получения текста подсказки.
     * У каждого типа подсказки выводится собственный текст.
     * CommandSender здесь нужен для перегрузки этого метода в ситуации, когда плагин расширяет дефолтные подсказки.
     * Когда CommandSender является игроком, для подсказки можно будет реализовать какой-нибудь фильтр по нику или типа
     * того.
     */
    public List<String> getText(CommandSender commandSender) {
        switch (type) {
            // Обычный тип подсказки. Примером может являться слово "remove".
            case "TEXT", "NUMBER" -> {
                return Lists.newArrayList(text);
            }
            // Подсказка такого типа предложит к вводу ники всех онлайн игроков (при этом текст подсказки не используется).
            case "PLAYER" -> {
                return Bukkit.getServer().getOnlinePlayers().stream().map(Player::getName).toList();
            }
            // Если тип подсказки не формирует список, тогда возвращаем пустой список предложений.
            default -> {
                return List.of();
            }
        }
    }
}
