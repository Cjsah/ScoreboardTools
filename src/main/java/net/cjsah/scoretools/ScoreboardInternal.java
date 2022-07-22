package net.cjsah.scoretools;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ScoreboardInternal {
    private final Map<Integer, SlotInternalImpl> internals = new HashMap<>();
    private final Scoreboard scoreboard;

    public ScoreboardInternal(MinecraftServer server) {
        this.scoreboard = server.getScoreboard();
    }

    public void add(int slot, ScoreboardObjective objective) {
        this.getOrCreateAndExecute(slot, (impl) -> impl.list.add(objective));
    }

    public void remove(int slot, ScoreboardObjective objective) {
        this.getOrCreateAndExecute(slot, (impl) -> impl.list.remove(objective));
    }

    public void setInternal(int slot, int internal) {
        this.getOrCreateAndExecute(slot, (impl) -> impl.internal = internal);
    }

    private void setIndex(int slot, int index) {
        this.getOrCreateAndExecute(slot, (impl) -> impl.index = index);
    }

    private void setProcess(int slot, int process) {
        this.getOrCreateAndExecute(slot, (impl) -> impl.tick = process);
    }

    private void getOrCreateAndExecute(int slot, Consumer<SlotInternalImpl> consumer) {
        SlotInternalImpl impl = this.internals.getOrDefault(slot, this.internals.put(slot, new SlotInternalImpl(this.scoreboard, slot)));
        consumer.accept(impl);
    }

    public void readNbt(NbtCompound nbt) {
        for(int i = 0; i < 19; ++i) {
            if (nbt.contains("slot_" + i, 8)) {
                NbtCompound slot = nbt.getCompound("slot_" + i);
                NbtList list = slot.getList("contents", 8);
                for (int j = 0; j < list.size(); j++) {
                    String name = list.getString(j);
                    ScoreboardObjective objective = this.scoreboard.getNullableObjective(name);
                    this.add(i, objective);
                }
                this.setInternal(i, slot.getInt("internal"));
                this.setProcess(i, slot.getInt("process"));
                this.setIndex(i, slot.getInt("index"));
            }
        }
    }

    public void writeNbt(NbtCompound nbt) {
        this.internals.forEach((index, impl) -> {
            NbtCompound slot = new NbtCompound();
            NbtList list = new NbtList();
            for (ScoreboardObjective objective : impl.list) {
                list.add(NbtString.of(objective.getName()));
            }
            slot.put("contents", list);
            slot.putInt("internal", index);
            slot.putInt("process", impl.tick);
            slot.putInt("index", impl.index);
            nbt.put("slot_" + index, slot);
        });
    }

    public void tick() {
        this.internals.values().forEach(SlotInternalImpl::tick);
    }

    private static class SlotInternalImpl {
        final List<ScoreboardObjective> list = new ArrayList<>();
        final Scoreboard scoreboard;
        final int slot;
        int internal;
        int index;
        int tick;

        SlotInternalImpl(Scoreboard scoreboard, int slot) {
            this.scoreboard = scoreboard;
            this.slot = slot;
        }

        void tick() {
            if (this.internal > 0 && this.list.size() > 1 && ++this.tick == this.internal) {
                this.scoreboard.setObjectiveSlot(this.slot, this.list.get(this.updateIndex()));
                this.tick = 0;
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
            return this.list + ",slot:[" + this.slot + "],internal:[" + this.internal + "],index:[" + this.index + "]";
        }
    }

}
