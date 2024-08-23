package com.akon.chatvox;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChatVox implements ClientModInitializer {

	public static final String MOD_ID = "chatvox";
	static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final int DEFAULT_PORT = 50021;
	static ChatVoxConfig config = new ChatVoxConfig();
	static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	@Override
	public void onInitializeClient() {
		NotificationUtil.init();
		if (OSAdapter.get() == OSAdapter.UNSUPPORTED) {
			NotificationUtil.error("You are using an unsupported OS: " + System.getProperty("os.name") + "\nChat Vox currently only supports Windows", null);
			return;
		}
		ConfigManager.loadConfig();
		VoiceVoxLauncher.launch();
		ClientLifecycleEvents.CLIENT_STARTED.register(client -> VoiceVoxCommunicator.init());
	}
}
