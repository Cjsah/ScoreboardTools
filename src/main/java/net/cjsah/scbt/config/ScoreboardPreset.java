package net.cjsah.scbt.config;

import com.google.gson.annotations.Expose;

public class ScoreboardPreset {
    @Expose
    private String name;
    @Expose private String criteria;
    @Expose private String text;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCriteria() {
        return this.criteria;
    }

    public void setCriteria(String criteria) {
        this.criteria = criteria;
    }

    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
