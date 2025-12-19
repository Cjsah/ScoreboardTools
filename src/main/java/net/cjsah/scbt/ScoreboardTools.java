package net.cjsah.scbt;

import com.mojang.brigadier.context.CommandContext;
import net.cjsah.scbt.RecordType.ElytraFlyingDistanceRecordType;
import net.cjsah.scbt.RecordType.OnlineTimeRecordType;
import net.fabricmc.api.ModInitializer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.Objective;

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
