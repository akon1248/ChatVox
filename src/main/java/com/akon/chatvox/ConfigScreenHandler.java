package com.akon.chatvox;

import com.akon.chatvox.data.Speaker;
import com.akon.chatvox.data.SpeakerStyle;
import com.mojang.datafixers.util.Pair;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.gui.entries.DropdownBoxEntry;
import net.minecraft.text.Text;

import java.util.Arrays;
import java.util.function.Function;

public class ConfigScreenHandler implements ModMenuApi {
	
	private static final Pair<Speaker, SpeakerStyle> EMPTY_ENTRY = Pair.of(null, null);

	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return parent -> {
			var builder = ConfigBuilder.create()
				.setParentScreen(parent)
				.setTitle(Text.literal("ChatVox Config"));
			var general = builder.getOrCreateCategory(Text.literal("General"));
			general.addEntry(builder.entryBuilder()
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
				);
			var currentSpeaker = VoiceVoxSpeakers.getById(ChatVox.config.speaker);
			var defaultSpeaker = VoiceVoxSpeakers.getById(3);
			Function<Pair<Speaker, SpeakerStyle>, Text> toTextFunc = pair -> {
				var speaker = pair.getFirst();
				if (speaker == null) {
					return Text.empty();
				}
				var style = pair.getSecond();
				if (style == null || speaker.styles().length == 1) {
					return Text.literal(speaker.name());
				}
				return Text.literal(speaker.name() + " (" + style.name() + ')');
			};
			general.addEntry(builder.entryBuilder()
				.startDropdownMenu(
					Text.literal("Speaker"),
					currentSpeaker == null ? EMPTY_ENTRY : currentSpeaker,
					s -> {
						if (s.isEmpty()) {
							return EMPTY_ENTRY;
						}
						var i = s.indexOf(" (");
						if (i == -1) {
							var speaker = VoiceVoxSpeakers.getSpeaker(s);
							if (speaker != null) {
								return Pair.of(speaker, speaker.styles()[0]);
							}
							return EMPTY_ENTRY;
						}
						if (s.charAt(s.length() - 1) != ')') {
							return EMPTY_ENTRY;
						}
						var speaker = VoiceVoxSpeakers.getSpeaker(s.substring(0, i));
						if (speaker == null) {
							return EMPTY_ENTRY;
						}
						var style = VoiceVoxSpeakers.getStyle(speaker.name(), s.substring(i + 2, s.length() - 1));
						if (style == null) {
							return EMPTY_ENTRY;
						}
						return Pair.of(speaker, style);
					},
					toTextFunc,
					new DropdownBoxEntry.DefaultSelectionCellCreator<>(toTextFunc)
				)
				.setDefaultValue(defaultSpeaker == null ? EMPTY_ENTRY : defaultSpeaker)
				.setTooltip(Text.literal("Speaker and style to use for voice synthesis"))
				.setSelections(VoiceVoxSpeakers.getSpeakers().stream()
					.flatMap(speaker -> Arrays.stream(speaker.styles())
						.map(style -> Pair.of(speaker, style))
					)
					.toList()
				)
				.setSaveConsumer(value -> {
					var style = value.getSecond();
					ChatVox.config.speaker = style == null ? 3 : style.id();
				})
				.build()
			);
			builder.setSavingRunnable(ConfigManager::saveConfig);
			return builder.build();
		};
	}
}
