package net.cjsah.scbt.data;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import net.cjsah.scbt.data.record.IScoreMapper;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ScoreboardToolContext {
    private final MinecraftServer server;
    private final ServerScoreboard scoreboard;
    private final Table<ScoreType, Objective, IScoreMapper> scores = HashBasedTable.create();
    private final ScoreboardScheduler scheduler;
    private final List<Runnable> dirtyListeners = new ArrayList<>();

    public ScoreboardToolContext(MinecraftServer server, ServerScoreboard scoreboard) {
        this.server = server;
        this.scoreboard = scoreboard;
        this.scheduler = new ScoreboardScheduler(scoreboard);
    }

    public void clearScoreBinds() {
        this.scores.clear();
        this.setDirty();
    }

    public void addScoreBind(String name, ScoreType type, IScoreMapper scoreMapper) {
        Objective objective = this.scoreboard.getObjective(name);
        if (objective == null) return;
        this.scores.put(type, objective, scoreMapper);
    }

    public void addScoreBind(Objective objective, ScoreType type, IScoreMapper scoreMapper) {
        this.scores.put(type, objective, scoreMapper);
        this.setDirty();
    }

    public void saveScoreBind(Consumer<Table.Cell<ScoreType, Objective, IScoreMapper>> saver) {
        for (Table.Cell<ScoreType, Objective, IScoreMapper> cell : this.scores.cellSet()) {
            saver.accept(cell);
        }
    }

    public void removeScoreBind(ScoreType scoreType, Objective objective) {
        this.scores.remove(scoreType, objective);
        this.setDirty();
    }

    public void initScoreScheduler(DisplaySlot slot, List<String> objectiveNames, int schedule, int internal, int index, boolean enable) {
        List<Objective> objectives = objectiveNames.stream()
            .map(this.scoreboard::getObjective)
            .filter(Objects::nonNull)
            .toList();
        this.scheduler.initScoreScheduler(slot, objectives, schedule, internal, index, enable);
        this.setDirty();
    }

    public void saveScoreScheduler(BiConsumer<DisplaySlot, ScoreboardScheduler.SlotScheduleImpl> saver) {
        for (Map.Entry<DisplaySlot, ScoreboardScheduler.SlotScheduleImpl> entry : this.scheduler.getSchedules().entrySet()) {
            saver.accept(entry.getKey(), entry.getValue());
        }
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

    public SavedData.Factory<ScoreboardToolSaveData> dataFactory() {
        return new SavedData.Factory<>(this::createData, this::createData, DataFixTypes.SAVED_DATA_SCOREBOARD);
    }

    private ScoreboardToolSaveData createData() {
        ScoreboardToolSaveData data = new ScoreboardToolSaveData(this);
        this.addDirtyListener(data::setDirty);
        return data;
    }

    private ScoreboardToolSaveData createData(CompoundTag compoundTag, HolderLookup.Provider provider) {
        return this.createData().load(compoundTag, provider);
    }

}
