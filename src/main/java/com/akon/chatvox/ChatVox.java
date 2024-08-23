package com.akon.chatvox;

import com.akon.chatvox.util.AudioUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.Mixer;

public class ChatVox implements ClientModInitializer {

	public static final String MOD_ID = "chatvox";
	static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final int DEFAULT_PORT = 50021;
	static ChatVoxConfig config = new ChatVoxConfig();
	static final Gson GSON = new GsonBuilder()
		.setPrettyPrinting()
		.registerTypeAdapterFactory(new TypeAdapterFactory() {
			@SuppressWarnings("unchecked")
			@Override
			public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
				if (!Mixer.Info.class.isAssignableFrom(type.getRawType())) {
					return null;
				}
				return (TypeAdapter<T>)new TypeAdapter<Mixer.Info>() {
					@Override
					public void write(JsonWriter out, Mixer.Info value) {
						GSON.toJson(value.getName(), String.class, out);
					}

					@Override
					public Mixer.Info read(JsonReader in) {
						String name = gson.fromJson(in, String.class);
						var mixerInfo = AudioUtil.getOutputDeviceByName(name);
						return mixerInfo == null ? AudioUtil.getDefaultOutputDevice() : mixerInfo;
					}
				};
			}
		})
		.create();

	@Override
	public void onInitializeClient() {
		NotificationUtil.init();
		if (OSAdapter.get() == OSAdapter.UNSUPPORTED) {
			NotificationUtil.error("You are using an unsupported OS: " + System.getProperty("os.name") + "\nChat Vox currently only supports Windows", null);
			return;
		}
		ConfigManager.loadConfig();
		VoiceVoxLauncher.launch();
		ClientLifecycleEvents.CLIENT_STARTED.register(client -> VoiceVoxClient.init());
	}

	public static boolean isEnabled() {
		return config.enabled && VoiceVoxClient.connected();
	}
}
