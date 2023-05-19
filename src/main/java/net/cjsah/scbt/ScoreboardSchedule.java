package net.cjsah.scbt;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.cjsah.scbt.config.LoopPreset;
import net.cjsah.scbt.config.ScoreboardPreset;
import net.minecraft.command.argument.ScoreboardCriterionArgumentType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ScoreboardCommand;
import net.minecraft.text.Text;

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

    public boolean contains(int slot, ScoreboardObjective objective) {
        SlotScheduleImpl impl = this.schedules.get(slot);
        return impl != null && impl.list.contains(objective);
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

    public void setEnable(int slot, boolean enable) {
        SlotScheduleImpl impl = this.schedules.get(slot);
        if (impl != null) impl.enable = enable;
    }

    private void setIndex(int slot, int index) {
        this.getOrCreateAndExecute(slot, (impl) -> impl.index = index);
    }

    public void setSchedule(int slot, int process) {
        this.getOrCreateAndExecute(slot, (impl) -> impl.schedule = process);
    }

    public void preset(List<ScoreboardObjective> objectives, LoopPreset preset) {
        int slot = Scoreboard.getDisplaySlotId(preset.getDisplay());
        SlotScheduleImpl impl = this.schedules.get(slot);
        if (impl == null) {
            impl = new SlotScheduleImpl(this.scoreboard, slot);
            this.schedules.put(slot, impl);
        }
        impl.list.clear();
        impl.list.addAll(objectives);
        impl.schedule = preset.getSchedule();
        impl.internal = 0;
        impl.index = 0;
        impl.enable = true;
    }

    public boolean disable(int slot) {
        SlotScheduleImpl impl = this.schedules.get(slot);
        if (impl != null) {
            boolean available = impl.available();
            impl.enable = false;
            return available;
        }
        return false;
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
                this.setEnable(i, slot.getBoolean("enable"));
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
            slot.putBoolean("enable", impl.enable);
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
        boolean enable;

        SlotScheduleImpl(Scoreboard scoreboard, int slot) {
            this.scoreboard = scoreboard;
            this.slot = slot;
            this.enable = true;
        }

        public boolean available() {
            return this.enable && this.schedule > 0 && this.list.size() > 1;
        }

        void tick() {
            if (this.available() && ++this.internal >= this.schedule) {
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
                    ",[slot:" + this.slot + "],[schedule:" + this.schedule + "],[index:" + this.index + "],[internal:" + this.internal + "],[enable:" + this.enable + "]";
        }
    }

}
