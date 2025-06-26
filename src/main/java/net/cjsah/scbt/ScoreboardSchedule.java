package net.cjsah.scbt;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ScoreboardSchedule {
    private final Map<ScoreboardDisplaySlot, SlotScheduleImpl> schedules = new HashMap<>();
    private final Scoreboard scoreboard;

    public ScoreboardSchedule(MinecraftServer server) {
        this.scoreboard = server.getScoreboard();
    }

    public boolean contains(ScoreboardDisplaySlot slot, ScoreboardObjective objective) {
        SlotScheduleImpl impl = this.schedules.get(slot);
        return impl != null && impl.list.contains(objective);
    }

    public void add(ScoreboardDisplaySlot slot, ScoreboardObjective objective) {
        this.getOrCreateAndExecute(slot, (impl) -> impl.list.add(objective));
    }

    public void addAll(ScoreboardDisplaySlot slot, List<ScoreboardObjective> objectives) {
        this.getOrCreateAndExecute(slot, (impl) -> impl.list.addAll(objectives));
    }

    public void remove(ScoreboardDisplaySlot slot, ScoreboardObjective objective) {
        this.getOrCreateAndExecute(slot, (impl) -> impl.remove(objective));
    }

    private void setInternal(ScoreboardDisplaySlot slot, int internal) {
        this.getOrCreateAndExecute(slot, (impl) -> impl.internal = internal);
    }

    public void setEnable(ScoreboardDisplaySlot slot, boolean enable) {
        SlotScheduleImpl impl = this.schedules.get(slot);
        if (impl != null) impl.enable = enable;
    }

    private void setIndex(ScoreboardDisplaySlot slot, int index) {
        this.getOrCreateAndExecute(slot, (impl) -> impl.index = index);
    }

    public void setSchedule(ScoreboardDisplaySlot slot, int process) {
        this.getOrCreateAndExecute(slot, (impl) -> impl.schedule = process);
    }

    private void getOrCreateAndExecute(ScoreboardDisplaySlot slot, Consumer<SlotScheduleImpl> consumer) {
        SlotScheduleImpl impl = this.schedules.get(slot);
        if (impl == null) {
            impl = new SlotScheduleImpl(this.scoreboard, slot);
            this.schedules.put(slot, impl);
        }
        consumer.accept(impl);
    }

    public void readNbt(NbtCompound nbt) {
        NbtCompound internal = nbt.getCompound("DisplayInternal");
        if (internal.isEmpty()) return;
        for (String key : internal.getKeys()) {
            ScoreboardDisplaySlot slot = ScoreboardDisplaySlot.CODEC.byId(key);
            if (slot != null) {
                NbtCompound compound = internal .getCompound(key);
                List<ScoreboardObjective> objectives = compound
                        .getList("contents", NbtElement.STRING_TYPE)
                        .stream()
                        .map(it -> this.scoreboard.getNullableObjective(it.asString()))
                        .toList();
                this.addAll(slot, objectives);
                this.setSchedule(slot, compound.getInt("schedule"));
                this.setInternal(slot, compound.getInt("internal"));
                this.setIndex(slot, compound.getInt("index"));
                this.setEnable(slot, compound.getBoolean("enable"));
            }
        }
    }

    public void writeNbt(NbtCompound nbt) {
        NbtCompound internal = new NbtCompound();
        this.schedules.forEach((slot, impl) -> {
            NbtCompound compound = new NbtCompound();
            NbtList list = new NbtList();
            for (ScoreboardObjective objective : impl.list) {
                list.add(NbtString.of(objective.getName()));
            }
            compound.put("contents", list);
            compound.putInt("schedule", impl.schedule);
            compound.putInt("internal", impl.internal);
            compound.putInt("index", impl.index);
            compound.putBoolean("enable", impl.enable);
            internal.put(slot.asString(), compound);
        });
        nbt.put("DisplayInternal", internal);
    }

    public void tick() {
        this.schedules.values().forEach(SlotScheduleImpl::tick);
    }

    private static class SlotScheduleImpl {
        final List<ScoreboardObjective> list = new ArrayList<>();
        final Scoreboard scoreboard;
        final ScoreboardDisplaySlot slot;
        int schedule;
        int internal;
        int index;
        boolean enable;

        SlotScheduleImpl(Scoreboard scoreboard, ScoreboardDisplaySlot slot) {
            this.scoreboard = scoreboard;
            this.slot = slot;
            this.enable = true;
        }

        public void remove(ScoreboardObjective objective) {
            this.list.remove(objective);
            if (this.list.size() == 1) {
                String name = this.list.getFirst().getName();
                String currentScore = this.scoreboard.getObjectiveForSlot(this.slot).getName();
                if (!currentScore.equals(name)) {
                    this.scoreboard.setObjectiveSlot(this.slot, this.list.getFirst());
                }
            }
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
