package net.cjsah.scbt;

import com.mojang.brigadier.context.CommandContext;
import net.cjsah.scbt.RecordType.ElytraFlyingDistanceRecordType;
import net.cjsah.scbt.RecordType.OnlineTimeRecordType;
import net.fabricmc.api.ModInitializer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ScoreboardTools implements ModInitializer {
    public static final Class<?> CARPET_PLAYER_CLASS;

    public static final String MINED_COUNT = "minedCount";
    public static final String PLACED_COUNT = "placedCount";
    public static final String ONLINE_TIME = "onlineTime";
    public static final String LEVEL_BOARD = "level";
    public static final String ELYTRA_FLYING_DISTANCE = "elytraFlyingDistance";

    public static boolean FakePlayerScore = true;
    public static final Set<Objective> MinedObjectives = new HashSet<>();
    public static final Set<Objective> PlacedObjectives = new HashSet<>();
    public static final Set<Objective> LevelObjectives = new HashSet<>();
    public static final Map<Objective, OnlineTimeRecordType> OnlineObjectives = new HashMap<>();
    public static final Map<Objective, ElytraFlyingDistanceRecordType> ElytraFlyingDistanceObjectives = new HashMap<>();

    @Override
    public void onInitialize() {
    }

    @SuppressWarnings("DataFlowIssue")
    public static void addScore(Player player, Set<Objective> objectives) {
        if (!carpetBotScore(player)) return;
        ServerScoreboard scoreboard = player.getServer().getScoreboard();
        objectives.forEach(it -> scoreboard.getOrCreatePlayerScore(player, it, true).increment());
    }

    @SuppressWarnings("DataFlowIssue")
    public static void setScore(Player player, Set<Objective> objectives, int score) {
        if (!carpetBotScore(player)) return;
        ServerScoreboard scoreboard = player.getServer().getScoreboard();
        objectives.forEach(it -> scoreboard.getOrCreatePlayerScore(player, it, true).set(score));
    }

    @SuppressWarnings("DataFlowIssue")
    public static void setScore(Player player, Objective objective, int score) {
        if (!carpetBotScore(player)) return;
        ServerScoreboard scoreboard = player.getServer().getScoreboard();
        scoreboard.getOrCreatePlayerScore(player, objective, true).set(score);
    }

    public static void readNbt(Scoreboard scoreboard, CompoundTag nbt) {
        FakePlayerScore = !nbt.contains("FakePlayerScore") || nbt.getBoolean("FakePlayerScore");
        CompoundTag bind = nbt.getCompound("ScoreboardBind");
        if (bind.isEmpty()) return;
        readObjectives(bind, scoreboard, MINED_COUNT, MinedObjectives);
        readObjectives(bind, scoreboard, PLACED_COUNT, PlacedObjectives);
        readObjectives(bind, scoreboard, LEVEL_BOARD, LevelObjectives);
        readMapObjectives(bind, scoreboard, ONLINE_TIME, OnlineObjectives, OnlineTimeRecordType.values());
        readMapObjectives(bind, scoreboard, ELYTRA_FLYING_DISTANCE, ElytraFlyingDistanceObjectives, ElytraFlyingDistanceRecordType.values());
    }

    public static void writeNbt(CompoundTag nbt) {
        CompoundTag compound = new CompoundTag();
        writeObjectives(compound, MINED_COUNT, MinedObjectives);
        writeObjectives(compound, PLACED_COUNT, PlacedObjectives);
        writeObjectives(compound, LEVEL_BOARD, LevelObjectives);
        writeMapObjectives(compound, ONLINE_TIME, OnlineObjectives);
        writeMapObjectives(compound, ELYTRA_FLYING_DISTANCE, ElytraFlyingDistanceObjectives);
        nbt.put("ScoreboardBind", compound);
        nbt.putBoolean("FakePlayerScore", FakePlayerScore);
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

    public static void feedbackCompleted(CommandContext<CommandSourceStack> context) {
        feedback(context, "Completed");
    }

    public static void feedback(CommandContext<CommandSourceStack> context, String text) {
        context.getSource().sendSystemMessage(Component.literal(text));
    }

    public static boolean carpetBotScore(Player player) {
        return FakePlayerScore || CARPET_PLAYER_CLASS == null || !CARPET_PLAYER_CLASS.isInstance(player);
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
