package net.cjsah.scoretools;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.server.MinecraftServer;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ScoreboardSchedule {
    private final Map<Integer, SlotScheduleImpl> schedules = new HashMap<>();
    private final Scoreboard scoreboard;

    public ScoreboardSchedule(MinecraftServer server) {
        this.scoreboard = server.getScoreboard();
    }

    public void add(int slot, ScoreboardObjective objective) {
        this.getOrCreateAndExecute(slot, (impl) -> impl.list.add(objective));
    }

    public void remove(int slot, ScoreboardObjective objective) {
        this.getOrCreateAndExecute(slot, (impl) -> impl.list.remove(objective));
    }

    private void setInternal(int slot, int internal) {
        this.getOrCreateAndExecute(slot, (impl) -> impl.internal = internal);
    }

    private void setIndex(int slot, int index) {
        this.getOrCreateAndExecute(slot, (impl) -> impl.index = index);
    }

    public void setSchedule(int slot, int process) {
        this.getOrCreateAndExecute(slot, (impl) -> impl.schedule = process);
    }

    @Nullable
    public SlotScheduleImpl clear(int slot) {
        SlotScheduleImpl impl = this.schedules.remove(slot);
        return impl.list.size() > 1 && impl.schedule > 0 ? impl : null;
    }

    private void getOrCreateAndExecute(int slot, Consumer<SlotScheduleImpl> consumer) {
        SlotScheduleImpl impl = this.schedules.get(slot);
        if (impl == null) {
            impl = new SlotScheduleImpl(this.scoreboard, slot);
            this.schedules.put(slot, impl);
        }
        consumer.accept(impl);
    }

    public void readNbt(NbtCompound nbt) {
        for(int i = 0; i < 19; ++i) {
            if (nbt.contains("slot_" + i, 10)) {
                NbtCompound slot = nbt.getCompound("slot_" + i);
                NbtList list = slot.getList("contents", 8);
                for (int j = 0; j < list.size(); j++) {
                    String name = list.getString(j);
                    ScoreboardObjective objective = this.scoreboard.getObjective(name);
                    this.add(i, objective);
                }
                this.setSchedule(i, slot.getInt("schedule"));
                this.setInternal(i, slot.getInt("internal"));
                this.setIndex(i, slot.getInt("index"));
            }
        }
    }

    public void writeNbt(NbtCompound nbt) {
        this.schedules.forEach((index, impl) -> {
            NbtCompound slot = new NbtCompound();
            NbtList list = new NbtList();
            for (ScoreboardObjective objective : impl.list) {
                list.add(NbtString.of(objective.getName()));
            }
            slot.put("contents", list);
            slot.putInt("schedule", impl.schedule);
            slot.putInt("internal", impl.internal);
            slot.putInt("index", impl.index);
            nbt.put("slot_" + index, slot);
        });
    }

    public void tick() {
        this.schedules.values().forEach(SlotScheduleImpl::tick);
    }

    private static class SlotScheduleImpl {
        final List<ScoreboardObjective> list = new ArrayList<>();
        final Scoreboard scoreboard;
        final int slot;
        int schedule;
        int internal;
        int index;

        SlotScheduleImpl(Scoreboard scoreboard, int slot) {
            this.scoreboard = scoreboard;
            this.slot = slot;
        }

        void tick() {
            if (this.schedule > 0 && this.list.size() > 1 && ++this.internal >= this.schedule) {
                this.scoreboard.setObjectiveSlot(this.slot, this.list.get(this.updateIndex()));
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
            return this.list.stream().map(ScoreboardObjective::getName).collect(Collectors.joining(",", "[", "]")) +
                    ",[slot:" + this.slot + "],[schedule:" + this.schedule + "],[index:" + this.index + "],[internal:" + this.internal + "]";
        }
    }

}
