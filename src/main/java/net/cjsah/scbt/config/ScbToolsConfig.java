package net.cjsah.scbt.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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
    private final List<LoopPreset> presets = new ArrayList<>();
    private final List<String> presetNames = new ArrayList<>();

    private ScbToolsConfig() {}

    public static void init() {
        if (!CONFIG_FILE.exists()) {
            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                writer.write("{\"presets\":[]}");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void update() {
        try (
                FileInputStream fis = new FileInputStream(CONFIG_FILE);
                InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8)
        ) {
            Config config = GSON.fromJson(isr, Config.class);
            this.presets.clear();
            this.presetNames.clear();
            this.presets.addAll(config.presets);
            for (LoopPreset preset : this.presets) {
                String name = preset.getName();
                if (!name.isEmpty() && this.presetNames.contains(name)) throw new IllegalArgumentException("Duplicate preset name '" + name + "'");
                else this.presetNames.add(name);
            }
            System.out.println(this.presets);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public LoopPreset getPreset(String name) throws CommandSyntaxException {
        List<LoopPreset> list = this.presets.stream().filter(preset -> name.equals(preset.getName())).toList();
        int size = list.size();
        switch (size) {
            case 0 -> throw NO_PRESET_EXCEPTION.create(name);
            default -> throw DUPLICATE_PRESET_EXCEPTION.create(name);
            case 1 -> {
                return list.get(0);
            }
        }
    }

    public List<String> getPresetsNames() {
        return this.presetNames;
    }

    static class Config {
        @Expose private List<LoopPreset> presets;
    }

}
