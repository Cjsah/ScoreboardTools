package net.cjsah.scbt;

import com.mojang.brigadier.context.CommandContext;
import net.cjsah.scbt.RecordType.ElytraFlyingDistanceRecordType;
import net.cjsah.scbt.RecordType.OnlineTimeRecordType;
import net.fabricmc.api.ModInitializer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class ScoreboardTools implements ModInitializer {
    public static final Class<?> CARPET_PLAYER_CLASS;

    public static final String MINED_COUNT = "minedCount";
    public static final String PLACED_COUNT = "placedCount";
    public static final String ONLINE_TIME = "onlineTime";
    public static final String LEVEL_BOARD = "level";
    public static final String ELYTRA_FLYING_DISTANCE = "elytraFlyingDistance";

    public static boolean FakePlayerScore = true;
    public static final Set<ScoreboardObjective> MinedObjectives = new HashSet<>();
    public static final Set<ScoreboardObjective> PlacedObjectives = new HashSet<>();
    public static final HashMap<ScoreboardObjective, OnlineTimeRecordType> OnlineObjectives = new HashMap<>();
    public static final HashMap<ScoreboardObjective, ElytraFlyingDistanceRecordType> ElytraFlyingDistanceObjectives = new HashMap<>();
    public static final HashSet<ScoreboardObjective> LevelObjectives = new HashSet<>();

    @Override
    public void onInitialize() {
    }

    @SuppressWarnings("DataFlowIssue")
    public static void addScore(PlayerEntity player, Set<ScoreboardObjective> objectives) {
        if (!carpetBotScore(player)) return;
        ServerScoreboard scoreboard = player.getServer().getScoreboard();
        objectives.forEach(it -> scoreboard.getOrCreateScore(player, it, true).incrementScore());
    }

    @SuppressWarnings("DataFlowIssue")
    public static void setScore(PlayerEntity player, Set<ScoreboardObjective> objectives, int score) {
        if (!carpetBotScore(player)) return;
        ServerScoreboard scoreboard = player.getServer().getScoreboard();
        objectives.forEach(it -> scoreboard.getOrCreateScore(player, it, true).setScore(score));
    }

    public static void readNbt(Scoreboard scoreboard, NbtCompound nbt) {
        FakePlayerScore = !nbt.contains("FakePlayerScore") || nbt.getBoolean("FakePlayerScore");
        NbtCompound bind = nbt.getCompound("ScoreboardBind");
        MinedObjectives.clear();
        PlacedObjectives.clear();
        OnlineObjectives.clear();
        LevelObjectives.clear();
        if (bind.isEmpty()) return;
        readObjectives(bind, scoreboard, MINED_COUNT, MinedObjectives);
        readObjectives(bind, scoreboard, PLACED_COUNT, PlacedObjectives);
        readObjectives(bind, scoreboard, LEVEL_BOARD, LevelObjectives);
        readMapObjectives(bind, scoreboard, ONLINE_TIME, OnlineObjectives, OnlineTimeRecordType.values());
        readMapObjectives(bind, scoreboard, ELYTRA_FLYING_DISTANCE, ElytraFlyingDistanceObjectives, ElytraFlyingDistanceRecordType.values());
    }

    public static void writeNbt(NbtCompound nbt) {
        NbtCompound compound = new NbtCompound();
        writeObjectives(compound, MINED_COUNT, MinedObjectives);
        writeObjectives(compound, PLACED_COUNT, PlacedObjectives);
        writeObjectives(compound, LEVEL_BOARD, LevelObjectives);
        writeMapObjectives(compound, ONLINE_TIME, OnlineObjectives);
        writeMapObjectives(compound, ELYTRA_FLYING_DISTANCE, ElytraFlyingDistanceObjectives);
        nbt.put("ScoreboardBind", compound);
        nbt.putBoolean("FakePlayerScore", FakePlayerScore);
    }

    private static <T> void readMapObjectives(
        NbtCompound nbt,
        Scoreboard scoreboard,
        String key,
        HashMap<ScoreboardObjective, T> map,
        T[] values
    ) {
        NbtCompound compound = nbt.getCompound(key);
        for (String name : compound.getKeys()) {
            ScoreboardObjective objective = scoreboard.getNullableObjective(name);
            if (objective != null) {
                map.put(objective, values[compound.getInt(name)]);
            }
        }
    }

    private static void readObjectives(NbtCompound nbt, Scoreboard scoreboard, String key, Collection<ScoreboardObjective> objectives) {
        for (NbtElement name : nbt.getList(key, NbtElement.STRING_TYPE)) {
            ScoreboardObjective objective = scoreboard.getNullableObjective(name.asString());
            if (objective != null) objectives.add(objective);
        }
    }

    private static <T extends Enum<T>> void writeMapObjectives(
        NbtCompound nbt,
        String key,
        HashMap<ScoreboardObjective, T> map
    ) {
        NbtCompound compound = new NbtCompound();
        for (ScoreboardObjective objective : map.keySet()) {
            compound.putInt(objective.getName(), map.get(objective).ordinal());
        }
        nbt.put(key, compound);
    }

    private static void writeObjectives(NbtCompound nbt, String key, Collection<ScoreboardObjective> objectives) {
        NbtList list = new NbtList();
        for (ScoreboardObjective objective : objectives) {
            list.add(NbtString.of(objective.getName()));
        }
        nbt.put(key, list);
    }

    public static void feedback(CommandContext<ServerCommandSource> context, String text) {
        context.getSource().sendFeedback(() -> Text.of(text), false);
    }

    public static boolean carpetBotScore(PlayerEntity player) {
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
