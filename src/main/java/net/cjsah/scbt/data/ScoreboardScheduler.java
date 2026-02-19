package net.cjsah.scbt.data;

import net.minecraft.server.MinecraftServer;
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
    private final Map<DisplaySlot, SlotScheduleImpl> schedules = new HashMap<>();
    private final ServerScoreboard scoreboard;

    public ScoreboardScheduler(ServerScoreboard scoreboard) {
        this.scoreboard = scoreboard;
    }

    public void initScoreScheduler(DisplaySlot slot, List<Objective> objectives, int schedule, int internal, int index, boolean enable) {
        SlotScheduleImpl impl = new SlotScheduleImpl(this.scoreboard, slot);
        impl.list.addAll(objectives);
        impl.schedule = schedule;
        impl.internal = internal;
        impl.index = index;
        impl.enable = enable;
        this.schedules.put(slot, impl);
    }

    public Map<DisplaySlot, SlotScheduleImpl> getSchedules() {
        return this.schedules;
    }

    public boolean contains(DisplaySlot slot, Objective objective) {
        SlotScheduleImpl impl = this.schedules.get(slot);
        return impl != null && impl.list.contains(objective);
    }

    public void add(DisplaySlot slot, Objective objective) {
        this.getOrCreateAndExecute(slot, (impl) -> impl.list.add(objective));
    }

    public void addAll(DisplaySlot slot, List<Objective> objectives) {
        this.getOrCreateAndExecute(slot, (impl) -> impl.list.addAll(objectives));
    }

    public void remove(DisplaySlot slot, Objective objective) {
        this.getOrCreateAndExecute(slot, (impl) -> impl.remove(objective));
    }

    public void setInternal(DisplaySlot slot, int internal) {
        this.getOrCreateAndExecute(slot, (impl) -> impl.internal = internal);
    }

    public void setEnable(DisplaySlot slot, boolean enable) {
        SlotScheduleImpl impl = this.schedules.get(slot);
        if (impl != null) impl.enable = enable;
    }

    public void setIndex(DisplaySlot slot, int index) {
        this.getOrCreateAndExecute(slot, (impl) -> impl.index = index);
    }

    public void setSchedule(DisplaySlot slot, int process) {
        this.getOrCreateAndExecute(slot, (impl) -> impl.schedule = process);
    }

    private void getOrCreateAndExecute(DisplaySlot slot, Consumer<SlotScheduleImpl> consumer) {
        SlotScheduleImpl impl = this.schedules.get(slot);
        if (impl == null) {
            impl = new SlotScheduleImpl(this.scoreboard, slot);
            this.schedules.put(slot, impl);
        }
        consumer.accept(impl);
    }

    public void tick() {
        this.schedules.values().forEach(SlotScheduleImpl::tick);
    }

    @Getter
    public static class SlotScheduleImpl {
        private final List<Objective> list = new ArrayList<>();
        private final Scoreboard scoreboard;
        private final DisplaySlot slot;
        private int schedule;
        private int internal;
        private int index;
        private boolean enable;

        SlotScheduleImpl(Scoreboard scoreboard, DisplaySlot slot) {
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

        void tick() {
            if (this.available() && ++this.internal >= this.schedule) {
                this.scoreboard.setDisplayObjective(this.slot, this.list.get(this.updateIndex()));
                this.internal = 0;
            }
        }

        int updateIndex() {
            int size = this.list.size();
            if (this.index >= size) this.index = 0;
            int origin = this.index;
            this.index = (this.index + 1) % size;
            return origin;
        }

        @Override
        public String toString() {
            return this.list.stream().map(Objective::getName).collect(Collectors.joining(",", "[", "]")) +
                ",[slot:" + this.slot + "],[schedule:" + this.schedule + "],[index:" + this.index + "],[internal:" + this.internal + "],[enable:" + this.enable + "]";
        }
    }

}
