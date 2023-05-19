package net.cjsah.scbt.config;

import com.google.gson.annotations.Expose;

public class ScoreboardPreset {
    @Expose private final String name;
    @Expose private final String criteria;
    @Expose private final String text;

    public ScoreboardPreset(String name, String criteria, String text) {
        this.name = name;
        this.criteria = criteria;
        this.text = text;
    }
    public String getName() {
        return this.name;
    }

    public String getCriteria() {
        return this.criteria;
    }

    public String getText() {
        return this.text;
    }

    @Override
    public String toString() {
        return "ScoreboardPreset{" +
                "name='" + name + '\'' +
                ", criteria='" + criteria + '\'' +
                ", text='" + text + '\'' +
                '}';
    }
}
