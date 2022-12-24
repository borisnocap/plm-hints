package ru.plm.hintcompleter;

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
        switch (type) {
            case "TEXT" -> {
                return Lists.newArrayList(text);
            }
            case "PLAYER" -> {
                return Bukkit.getServer().getOnlinePlayers().stream().map(Player::getName).toList();
            }
            default -> {
                return List.of();
            }
        }
    }
}
