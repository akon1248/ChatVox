package com.akon.chatvox;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import lombok.experimental.UtilityClass;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

@UtilityClass
public class ConfigManager {

	private final File CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve(ChatVox.MOD_ID + ".json").toFile();

	public void loadConfig() {
		if (CONFIG_FILE.exists()) {
			try (var reader = new FileReader(CONFIG_FILE)) {
				var config = ChatVox.GSON.fromJson(reader, ChatVoxConfig.class);
				if (config == null) {
					NotificationUtil.error("Failed to load config", null);
					return;
				}
				ChatVox.config = config;
			} catch (IOException | JsonSyntaxException | JsonIOException e) {
				NotificationUtil.error("Failed to load config", null, e);
			}
		} else {
			saveConfig();
		}
	}

	public void saveConfig() {
		try (var writer = new FileWriter(CONFIG_FILE)) {
			ChatVox.GSON.toJson(ChatVox.config, writer);
		} catch (IOException | JsonIOException e) {
			NotificationUtil.error("Failed to save config", null, e);
		}
	}

}
