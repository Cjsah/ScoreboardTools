package net.cjsah.scbt.data;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import net.cjsah.scbt.data.record.IScoreMapper;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class ScoreboardToolContext {
    public static final Class<?> CARPET_PLAYER_CLASS;

    private final ServerScoreboard scoreboard;
    private final ScoreboardScheduler scheduler;
    private boolean carpetBotScore = true;
    private final Table<ScoreType, Objective, IScoreMapper> scores = HashBasedTable.create();

    private boolean dirty = false;

    public ScoreboardToolContext(ServerScoreboard scoreboard) {
        this.scoreboard = scoreboard;
        this.scheduler = new ScoreboardScheduler(scoreboard, this);
    }

    public void setCarpetBotScore(boolean carpetBotScore) {
        this.carpetBotScore = carpetBotScore;
        this.setDirty();
    }

    public boolean isCarpetBotScore() {
        return this.carpetBotScore;
    }

    public boolean canScore(Player player) {
        return this.carpetBotScore || CARPET_PLAYER_CLASS == null || !CARPET_PLAYER_CLASS.isInstance(player);
    }

    public void addScore(Player player, ScoreType scoreType) {
        if (!this.canScore(player)) return;
        ServerScoreboard scoreboard = player.level().getServer().getScoreboard();
        Map<Objective, IScoreMapper> row = this.scores.row(scoreType);
        row.keySet().forEach(it -> scoreboard.getOrCreatePlayerScore(player, it, true).increment());
    }

    public void setOriginScore(Player player, ScoreType scoreType, int score) {
        if (!this.canScore(player)) return;
        ServerScoreboard scoreboard = player.level().getServer().getScoreboard();
        Map<Objective, IScoreMapper> row = this.scores.row(scoreType);
        row.forEach((objective, scoreMapper) ->
            scoreboard.getOrCreatePlayerScore(player, objective, true).set(scoreMapper.mapValue(score)));
    }

    public void addScoreBind(Objective objective, ScoreType type, IScoreMapper scoreMapper) {
        this.scores.put(type, objective, scoreMapper);
        this.setDirty();
    }

    public void removeScoreBind(ScoreType scoreType, Objective objective) {
        this.scores.remove(scoreType, objective);
        this.setDirty();
    }

    public ScoreboardScheduler getScheduler() {
        return this.scheduler;
    }

    protected void setDirty() {
        this.dirty = true;
    }

    public void storeToSaveDataIfDirty(ScoreboardToolSaveData saveData) {
        if (this.dirty) {
            this.dirty = false;
            saveData.setData(this.store());
        }
    }

    public void load(ScoreboardToolSaveData.Packed packed) {
        this.carpetBotScore = packed.fakePlayerScore();
        this.scores.clear();
        this.scheduler.clear();
        for (var bind : packed.binds()) {
            ScoreType scoreType = ScoreType.getByName(bind.type());
            if (scoreType == null) continue;
            IScoreMapper scoreMapper = scoreType.getScoreMapper(bind.record());
            Objective objective = this.scoreboard.getObjective(bind.name());
            if (objective == null) return;
            this.scores.put(scoreType, objective, scoreMapper);
        }
        for (var entry : packed.schedules().entrySet()) {
            ScoreboardToolSaveData.SchedulePacked schedule = entry.getValue();
            List<Objective> objectives = schedule.contents().stream()
                .map(this.scoreboard::getObjective)
                .filter(Objects::nonNull)
                .toList();
            this.scheduler.initScoreScheduler(
                entry.getKey(),
                objectives,
                schedule.schedule(),
                schedule.internal(),
                schedule.index(),
                schedule.enable()
            );
        }
    }

    private ScoreboardToolSaveData.Packed store() {
        List<ScoreboardToolSaveData.BindPacked> binds = this.scores.cellSet()
            .stream()
            .map(it ->
                new ScoreboardToolSaveData.BindPacked(
                    it.getColumnKey().getName(),
                    it.getRowKey().getName(),
                    it.getValue().saveId()
                ))
            .toList();
        Map<DisplaySlot, ScoreboardToolSaveData.SchedulePacked> schedules = this.scheduler.getSchedules()
            .entrySet()
            .stream()
            .collect(Collectors.toMap(Map.Entry::getKey, it -> {
                ScoreboardScheduler.SlotScheduler impl = it.getValue();
                List<String> contents = impl.getList().stream().map(Objective::getName).toList();
                return new ScoreboardToolSaveData.SchedulePacked(
                    contents,
                    impl.getSchedule(),
                    impl.getInternal(),
                    impl.getIndex(),
                    impl.isEnable()
                );
            }));
        return new ScoreboardToolSaveData.Packed(this.carpetBotScore, binds, schedules);
    }

    static {
        Class<?> clazz;
        try {
            clazz = Class.forName("carpet.patches.EntityPlayerMPFake");
        } catch (ClassNotFoundException e) {
            clazz = null;
        }
        CARPET_PLAYER_CLASS = clazz;
    }
}
