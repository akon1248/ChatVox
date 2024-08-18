package com.akon.chatvox;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChatVox implements ClientModInitializer {

	public static final String MOD_ID = "chatvox";
	static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final int DEFAULT_PORT = 50021;
	static ChatVoxConfig config = new ChatVoxConfig();

	@Override
	public void onInitializeClient() {
		ErrorReporter.init();
		if (VoiceVoxFinder.get() == VoiceVoxFinder.UNSUPPORTED) {
			ErrorReporter.error("You are using an unsupported OS: " + System.getProperty("os.name") + "\nChat Vox currently only supports Windows");
			return;
		}
		ConfigManager.loadConfig();
		VoiceVoxLauncher.launch();
	}
}
