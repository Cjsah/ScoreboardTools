package net.cjsah.scbt.data;

import net.minecraft.server.ServerScoreboard;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ScoreboardScheduler {
    private final Map<DisplaySlot, SlotScheduler> schedules = new HashMap<>();
    private final ServerScoreboard scoreboard;
    private final ScoreboardToolContext context;

    public ScoreboardScheduler(ServerScoreboard scoreboard, ScoreboardToolContext context) {
        this.scoreboard = scoreboard;
        this.context = context;
    }

    public void initScoreScheduler(DisplaySlot slot, List<Objective> objectives, int schedule, int internal, int index, boolean enable) {
        SlotScheduler scheduler = new SlotScheduler(this.scoreboard, slot);
        scheduler.list.addAll(objectives);
        scheduler.schedule = schedule;
        scheduler.internal = internal;
        scheduler.index = index;
        scheduler.enable = enable;
        this.schedules.put(slot, scheduler);
    }

    public Map<DisplaySlot, SlotScheduler> getSchedules() {
        return this.schedules;
    }

    public void clear() {
        this.schedules.clear();
    }

    public boolean contains(DisplaySlot slot, Objective objective) {
        SlotScheduler scheduler = this.schedules.get(slot);
        return scheduler != null && scheduler.list.contains(objective);
    }

    public void add(DisplaySlot slot, Objective objective) {
        this.getOrCreateAndExecute(slot, scheduler -> scheduler.list.add(objective));
        this.context.setDirty();
    }

    public void remove(DisplaySlot slot, Objective objective) {
        this.getOrCreateAndExecute(slot, scheduler -> scheduler.remove(objective));
        this.context.setDirty();
    }

    public void setEnable(DisplaySlot slot, boolean enable) {
        SlotScheduler scheduler = this.schedules.get(slot);
        if (scheduler != null) scheduler.enable = enable;
        this.context.setDirty();
    }

    public void setSchedule(DisplaySlot slot, int process) {
        this.getOrCreateAndExecute(slot, scheduler -> scheduler.schedule = process);
        this.context.setDirty();
    }

    private void getOrCreateAndExecute(DisplaySlot slot, Consumer<SlotScheduler> consumer) {
        SlotScheduler scheduler = this.schedules.get(slot);
        if (scheduler == null) {
            scheduler = new SlotScheduler(this.scoreboard, slot);
            this.schedules.put(slot, scheduler);
        }
        consumer.accept(scheduler);
    }

    public void tick() {
        this.schedules.values().forEach(it -> it.tick(this.context::setDirty));
    }

    public static class SlotScheduler {
        private final List<Objective> list = new ArrayList<>();
        private final Scoreboard scoreboard;
        private final DisplaySlot slot;
        private int schedule;
        private int internal;
        private int index;
        private boolean enable;

        SlotScheduler(Scoreboard scoreboard, DisplaySlot slot) {
            this.scoreboard = scoreboard;
            this.slot = slot;
            this.enable = true;
        }

        public void remove(Objective objective) {
            this.list.remove(objective);
            if (this.list.size() == 1) {
                String name = this.list.getFirst().getName();
                Objective currentObjective = this.scoreboard.getDisplayObjective(this.slot);
                if (currentObjective == null || !currentObjective.getName().equals(name)) {
                    this.scoreboard.setDisplayObjective(this.slot, this.list.getFirst());
                }
            }
        }

        public boolean available() {
            return this.enable && this.schedule > 0 && this.list.size() > 1;
        }

        void tick(Runnable dirty) {
            if (!this.available()) return;
            if (++this.internal >= this.schedule) {
                this.scoreboard.setDisplayObjective(this.slot, this.list.get(this.updateIndex()));
                this.internal = 0;
            }
            dirty.run();
        }

        int updateIndex() {
            int size = this.list.size();
            if (this.index >= size) this.index = 0;
            int origin = this.index;
            this.index = (this.index + 1) % size;
            return origin;
        }

        public List<Objective> getList() {
            return this.list;
        }

        public DisplaySlot getSlot() {
            return this.slot;
        }

        public int getSchedule() {
            return this.schedule;
        }

        public int getInternal() {
            return this.internal;
        }

        public int getIndex() {
            return this.index;
        }

        public boolean isEnable() {
            return this.enable;
        }

        @Override
        public String toString() {
            return this.list.stream().map(Objective::getName).collect(Collectors.joining(",", "[", "]")) +
                ",[slot:" + this.slot + "],[schedule:" + this.schedule + "],[index:" + this.index + "],[internal:" + this.internal + "],[enable:" + this.enable + "]";
        }
    }

}
