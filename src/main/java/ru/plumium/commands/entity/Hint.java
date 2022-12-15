package ru.plumium.commands.entity;

import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
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
        return childHints.size() != 0;
    }

    public Hint addChildHint(Hint childHint) {
        childHints.add(childHint);
        return this;
    }

    public List<String> getText() {
        // В зависимости от типа аргумента список подсказок формируется по разному.
        switch (type) {
            // Если аргумент текстовый, то просто возвращается список из одной подсказки.
            case "TEXT" -> {
                return Lists.newArrayList(text);
            }
            // Если аругмент представляет из себя никнейм игрока, тогда формируется список онлайн игроков сервера.
            case "PLAYER" -> {
                return Bukkit.getServer().getOnlinePlayers().stream().map(Player::getName).toList();
            }
            // Если аргумент имеет тип, для которого нет особого способа обработки аргументов, возвращается пустой список.
            default -> {
                return List.of();
            }
        }
    }
}
