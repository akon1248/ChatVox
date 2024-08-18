package com.akon.chatvox;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.experimental.UtilityClass;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

@UtilityClass
public class ConfigManager {

	private final File CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve(ChatVox.MOD_ID + ".json").toFile();
	private final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	public void loadConfig() {
		if (CONFIG_FILE.exists()) {
			try (var reader = new FileReader(CONFIG_FILE)) {
				ChatVoxConfig config = GSON.fromJson(reader, ChatVoxConfig.class);
				if (config == null) {
					ErrorReporter.error("Failed to load config");
					return;
				}
				ChatVox.config = config;
			} catch (IOException e) {
				ErrorReporter.error("Failed to load config", e);
			}
		} else {
			saveConfig();
		}
	}

	public void saveConfig() {
		try (var writer = new FileWriter(CONFIG_FILE)) {
			GSON.toJson(ChatVox.config, writer);
		} catch (IOException e) {
			ErrorReporter.error("Failed to save config", e);
		}
	}

}
