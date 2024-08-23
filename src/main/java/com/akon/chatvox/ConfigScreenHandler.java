package com.akon.chatvox;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import net.minecraft.text.Text;

public class ConfigScreenHandler implements ModMenuApi {

	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return parent -> {
			var builder = ConfigBuilder.create()
				.setParentScreen(parent)
				.setTitle(Text.literal("ChatVox Config"));
			builder.getOrCreateCategory(Text.literal("General"))
				.addEntry(builder.entryBuilder()
					.startIntField(Text.literal("Port"), ChatVox.config.port)
					.setTooltip(Text.literal("VOICEVOX Engine Server Port"))
					.setDefaultValue(ChatVox.DEFAULT_PORT)
					.setSaveConsumer(value -> ChatVox.config.port = value)
					.build()
				)
				.addEntry(builder.entryBuilder()
					.startStrField(Text.literal("VoiceVox Path"), ChatVox.config.voiceVoxPath)
					.setTooltip(Text.literal("Path to VoiceVox executable\nLeave blank to use detected path"))
					.setDefaultValue("")
					.setSaveConsumer(value -> ChatVox.config.voiceVoxPath = value)
					.build()
				)
				.addEntry(builder.entryBuilder()
					.startIntField(Text.literal("Speaker"), ChatVox.config.speaker)
					.setTooltip(Text.literal("Speaker ID"))
					.setDefaultValue(3)
					.setSaveConsumer(value -> ChatVox.config.speaker = value)
					.build()
				)
			;
			builder.setSavingRunnable(ConfigManager::saveConfig);
			return builder.build();
		};
	}
}
