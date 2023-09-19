package net.cjsah.scbt.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.cjsah.scbt.ScoreboardTools;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.Text;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

public final class ScbToolsConfig {
    private static final File CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("scbt.json").toFile();
    private static final Gson GSON = new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .serializeNulls()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();
    private static final ScbToolsConfig INSTANCE = new ScbToolsConfig();
    private static final DynamicCommandExceptionType NO_PRESET_EXCEPTION = new DynamicCommandExceptionType(name ->
            Text.of("No preset named '" + name + "'"));
    private static final DynamicCommandExceptionType DUPLICATE_PRESET_EXCEPTION = new DynamicCommandExceptionType(name ->
            Text.of("Duplicate preset named '" + name + "'"));

    public static ScbToolsConfig getInstance() {
        return INSTANCE;
    }
    private Config config;

    private ScbToolsConfig() {
        if (!CONFIG_FILE.exists()) {
            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                writer.write("{\"presets\":[],\"carpet_player_score\":false}");
            } catch (IOException e) {
                ScoreboardTools.LOGGER.error("Failed to create config file", e);
            }
        }
        this.config = new Config() {{
            this.presets = Collections.emptyList();
            this.carpetPlayerScore = false;
        }};
    }

    public void update() {
        try (
                FileInputStream fis = new FileInputStream(CONFIG_FILE);
                InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8)
        ) {
            this.config = GSON.fromJson(isr, Config.class);
        } catch (IOException e) {
            ScoreboardTools.LOGGER.error("Failed to load config file", e);
        }
    }

    public LoopPreset getPreset(String name) throws CommandSyntaxException {
        List<LoopPreset> list = this.config.presets.stream().filter(preset -> name.equals(preset.getName())).toList();
        int size = list.size();
        switch (size) {
            case 0 -> throw NO_PRESET_EXCEPTION.create(name);
            default -> throw DUPLICATE_PRESET_EXCEPTION.create(name);
            case 1 -> {
                return list.get(0);
            }
        }
    }

    public boolean isCarpetPlayerScore() {
        return this.config.carpetPlayerScore;
    }

    static class Config {
        @Expose protected List<LoopPreset> presets;
        @Expose protected boolean carpetPlayerScore;

    }

}
