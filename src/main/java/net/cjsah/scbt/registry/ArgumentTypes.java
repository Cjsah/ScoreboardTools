package net.cjsah.scbt.registry;

import com.mojang.brigadier.arguments.ArgumentType;
import net.cjsah.scbt.ScoreboardTools;
import net.cjsah.scbt.command.argument.ScoreboardPresetArgumentType;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;

import java.util.function.Supplier;

public class ArgumentTypes {

    public static void registry() {
        register("preset", ScoreboardPresetArgumentType.class, ScoreboardPresetArgumentType::scoreboardPreset);
    }

    @SuppressWarnings("SameParameterValue")
    private static <T extends ArgumentType<?>> void register(String name, Class<T> clazz, Supplier<T> typeSupplier) {
        ArgumentTypeRegistry.registerArgumentType(ScoreboardTools.of(name), clazz, ConstantArgumentSerializer.of(typeSupplier));
    }

}
