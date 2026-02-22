package net.cjsah.scbt.data;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import net.cjsah.scbt.data.record.IScoreMapper;
import net.cjsah.scbt.fake.ScoreboardToolFake;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ScoreboardSaveData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ScoreboardToolContext {
    public static final Class<?> CARPET_PLAYER_CLASS;
    public static final SavedDataType<ScoreboardToolSaveData> TYPE = new SavedDataType<>(
        "scoreboard_tool_data", context ->
        ((ScoreboardToolFake) context.levelOrThrow().getServer()).scbt$getContext().createData(),
        context -> {
            ScoreboardToolContext scoreContext = ((ScoreboardToolFake) context.levelOrThrow().getServer()).scbt$getContext();
            return ScoreboardToolSaveData.Packed.CODEC.xmap(scoreContext::createData, ScoreboardToolSaveData::pack);
        }, DataFixTypes.SAVED_DATA_SCOREBOARD
    );

    private final ServerScoreboard scoreboard;
    private final Table<ScoreType, Objective, IScoreMapper> scores = HashBasedTable.create();
    private final ScoreboardScheduler scheduler;
    private final List<Runnable> dirtyListeners = new ArrayList<>();
    private boolean carpetBotScore = true;
    private boolean dirty = false;

    public ScoreboardToolContext(ServerScoreboard scoreboard) {
        this.scoreboard = scoreboard;
        this.scheduler = new ScoreboardScheduler(scoreboard, this);
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
                ScoreboardScheduler.SlotScheduleImpl impl = it.getValue();
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

    public void storeToSaveDataIfDirty(ScoreboardToolSaveData saveData) {
        if (this.dirty) {
            this.dirty = false;
            saveData.setData(this.store());
        }
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

    public void addDirtyListener(Runnable runnable) {
        this.dirtyListeners.add(runnable);
    }

    protected void setDirty() {
        for (Runnable runnable : this.dirtyListeners) {
            runnable.run();
        }
    }

    private ScoreboardToolSaveData createData() {
        ScoreboardToolSaveData data = new ScoreboardToolSaveData(this);
        this.addDirtyListener(data::setDirty);
        return data;
    }

    private ScoreboardToolSaveData createData(ScoreboardToolSaveData.Packed packed) {
        ScoreboardToolSaveData data = this.createData();
        data.load(packed);
        return data;
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
