package net.cjsah.scbt;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ScoreboardSchedule {
    private final Map<DisplaySlot, SlotScheduleImpl> schedules = new HashMap<>();
    private final Scoreboard scoreboard;

    public ScoreboardSchedule(MinecraftServer server) {
        this.scoreboard = server.getScoreboard();
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

    private void setInternal(DisplaySlot slot, int internal) {
        this.getOrCreateAndExecute(slot, (impl) -> impl.internal = internal);
    }

    public void setEnable(DisplaySlot slot, boolean enable) {
        SlotScheduleImpl impl = this.schedules.get(slot);
        if (impl != null) impl.enable = enable;
    }

    private void setIndex(DisplaySlot slot, int index) {
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

    public void readNbt(CompoundTag nbt) {
        CompoundTag internal = nbt.getCompound("DisplayInternal");
        if (internal.isEmpty()) return;
        for (String key : internal.getAllKeys()) {
            DisplaySlot slot = DisplaySlot.CODEC.byName(key);
            if (slot != null) {
                CompoundTag compound = internal .getCompound(key);
                List<Objective> objectives = compound
                        .getList("contents", Tag.TAG_STRING)
                        .stream()
                        .map(it -> this.scoreboard.getObjective(it.getAsString()))
                        .toList();
                this.addAll(slot, objectives);
                this.setSchedule(slot, compound.getInt("schedule"));
                this.setInternal(slot, compound.getInt("internal"));
                this.setIndex(slot, compound.getInt("index"));
                this.setEnable(slot, compound.getBoolean("enable"));
            }
        }
    }

    public void writeNbt(CompoundTag nbt) {
        CompoundTag internal = new CompoundTag();
        this.schedules.forEach((slot, impl) -> {
            CompoundTag compound = new CompoundTag();
            ListTag list = new ListTag();
            for (Objective objective : impl.list) {
                list.add(StringTag.valueOf(objective.getName()));
            }
            compound.put("contents", list);
            compound.putInt("schedule", impl.schedule);
            compound.putInt("internal", impl.internal);
            compound.putInt("index", impl.index);
            compound.putBoolean("enable", impl.enable);
            internal.put(slot.getSerializedName(), compound);
        });
        nbt.put("DisplayInternal", internal);
    }

    public void tick() {
        this.schedules.values().forEach(SlotScheduleImpl::tick);
    }

    private static class SlotScheduleImpl {
        final List<Objective> list = new ArrayList<>();
        final Scoreboard scoreboard;
        final DisplaySlot slot;
        int schedule;
        int internal;
        int index;
        boolean enable;

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
