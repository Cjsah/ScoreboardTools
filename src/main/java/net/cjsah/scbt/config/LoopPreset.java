package net.cjsah.scbt.config;

import com.google.gson.annotations.Expose;

import java.util.Arrays;

public class LoopPreset {
    @Expose private final String name;
    @Expose private final String display;
    @Expose private final int schedule;
    @Expose private final ScoreboardPreset[] scoreboards;

    public LoopPreset(String name, String display, int schedule, ScoreboardPreset[] scoreboards) {
        this.name = name;
        this.display = display;
        this.schedule = schedule;
        this.scoreboards = scoreboards;
    }

    public String getName() {
        return this.name;
    }

    public String getDisplay() {
        return this.display;
    }

    public int getSchedule() {
        return this.schedule;
    }

    public ScoreboardPreset[] getScoreboards() {
        return this.scoreboards;
    }

    @Override
    public String toString() {
        return "LoopPreset{" +
                "name='" + name + '\'' +
                ", display='" + display + '\'' +
                ", schedule=" + schedule +
                ", scoreboards=" + Arrays.toString(scoreboards) +
                '}';
    }
}
