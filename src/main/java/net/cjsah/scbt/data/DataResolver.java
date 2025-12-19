package net.cjsah.scbt.data;

import net.cjsah.scbt.RecordType.ElytraFlyingDistanceRecordType;
import net.cjsah.scbt.RecordType.OnlineTimeRecordType;
import net.cjsah.scbt.ScoreboardSchedule;
import net.cjsah.scbt.ScoreboardTools;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class DataResolver {
    public static void readNbt(Scoreboard scoreboard, CompoundTag nbt) {
        ScoreboardTools.FakePlayerScore = !nbt.contains("FakePlayerScore") || nbt.getBoolean("FakePlayerScore");
        CompoundTag bind = nbt.getCompound("ScoreboardBind");
        if (bind.isEmpty()) return;
        readObjectives(bind, scoreboard, ScoreboardTools.MINED_COUNT, ScoreboardTools.MinedObjectives);
        readObjectives(bind, scoreboard, ScoreboardTools.PLACED_COUNT, ScoreboardTools.PlacedObjectives);
        readObjectives(bind, scoreboard, ScoreboardTools.LEVEL_BOARD, ScoreboardTools.LevelObjectives);
        readMapObjectives(bind, scoreboard, ScoreboardTools.ONLINE_TIME, ScoreboardTools.OnlineObjectives, OnlineTimeRecordType.values());
        readMapObjectives(bind, scoreboard, ScoreboardTools.ELYTRA_FLYING_DISTANCE, ScoreboardTools.ElytraFlyingDistanceObjectives, ElytraFlyingDistanceRecordType.values());
    }

    public static void writeNbt(CompoundTag nbt) {
        CompoundTag compound = new CompoundTag();
        writeObjectives(compound, ScoreboardTools.MINED_COUNT, ScoreboardTools.MinedObjectives);
        writeObjectives(compound, ScoreboardTools.PLACED_COUNT, ScoreboardTools.PlacedObjectives);
        writeObjectives(compound, ScoreboardTools.LEVEL_BOARD, ScoreboardTools.LevelObjectives);
        writeMapObjectives(compound, ScoreboardTools.ONLINE_TIME, ScoreboardTools.OnlineObjectives);
        writeMapObjectives(compound, ScoreboardTools.ELYTRA_FLYING_DISTANCE, ScoreboardTools.ElytraFlyingDistanceObjectives);
        nbt.put("ScoreboardBind", compound);
        nbt.putBoolean("FakePlayerScore", ScoreboardTools.FakePlayerScore);
    }

    private static <T> void readMapObjectives(CompoundTag nbt, Scoreboard scoreboard, String key, Map<Objective, T> map, T[] values) {
        map.clear();
        CompoundTag compound = nbt.getCompound(key);
        for (String name : compound.getAllKeys()) {
            Objective objective = scoreboard.getObjective(name);
            if (objective != null) {
                map.put(objective, values[compound.getInt(name)]);
            }
        }
    }

    private static void readObjectives(CompoundTag nbt, Scoreboard scoreboard, String key, Collection<Objective> objectives) {
        objectives.clear();
        for (Tag name : nbt.getList(key, Tag.TAG_STRING)) {
            Objective objective = scoreboard.getObjective(name.getAsString());
            if (objective != null) objectives.add(objective);
        }
    }

    private static <T extends Enum<T>> void writeMapObjectives(CompoundTag nbt, String key, Map<Objective, T> map) {
        CompoundTag compound = new CompoundTag();
        for (Objective objective : map.keySet()) {
            compound.putInt(objective.getName(), map.get(objective).ordinal());
        }
        nbt.put(key, compound);
    }

    private static void writeObjectives(CompoundTag nbt, String key, Collection<Objective> objectives) {
        ListTag list = new ListTag();
        for (Objective objective : objectives) {
            list.add(StringTag.valueOf(objective.getName()));
        }
        nbt.put(key, list);
    }

    public static class Schedule {
        public static void readNbt(ScoreboardSchedule schedule, CompoundTag nbt) {
            CompoundTag internal = nbt.getCompound("DisplayInternal");
            if (internal.isEmpty()) return;
            Scoreboard scoreboard = schedule.getScoreboard();
            for (String key : internal.getAllKeys()) {
                DisplaySlot slot = DisplaySlot.CODEC.byName(key);
                if (slot == null) continue;
                CompoundTag compound = internal .getCompound(key);
                List<Objective> objectives = compound
                    .getList("contents", Tag.TAG_STRING)
                    .stream()
                    .map(it -> scoreboard.getObjective(it.getAsString()))
                    .toList();
                schedule.addAll(slot, objectives);
                schedule.setSchedule(slot, compound.getInt("schedule"));
                schedule.setInternal(slot, compound.getInt("internal"));
                schedule.setIndex(slot, compound.getInt("index"));
                schedule.setEnable(slot, compound.getBoolean("enable"));
            }
        }

        public static void writeNbt(ScoreboardSchedule schedule, CompoundTag nbt) {
            CompoundTag internal = new CompoundTag();
            schedule.getSchedules().forEach((slot, impl) -> {
                CompoundTag compound = new CompoundTag();
                ListTag list = new ListTag();
                for (Objective objective : impl.getList()) {
                    list.add(StringTag.valueOf(objective.getName()));
                }
                compound.put("contents", list);
                compound.putInt("schedule", impl.getSchedule());
                compound.putInt("internal", impl.getInternal());
                compound.putInt("index", impl.getIndex());
                compound.putBoolean("enable", impl.isEnable());
                internal.put(slot.getSerializedName(), compound);
            });
            nbt.put("DisplayInternal", internal);
        }

    }

}
