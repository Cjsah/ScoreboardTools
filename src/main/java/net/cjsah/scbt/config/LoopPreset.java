package net.cjsah.scbt.config;

import com.google.gson.annotations.Expose;

import java.util.List;

public class LoopPreset {
    @Expose
    private String name;
    @Expose private String display;
    @Expose private int schedule;
    @Expose private List<ScoreboardPreset> scoreboards;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplay() {
        return this.display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    public int getSchedule() {
        return this.schedule;
    }

    public void setSchedule(int schedule) {
        this.schedule = schedule;
    }

    public List<ScoreboardPreset> getScoreboards() {
        return this.scoreboards;
    }

    public void setScoreboards(List<ScoreboardPreset> scoreboards) {
        this.scoreboards = scoreboards;
    }
}
