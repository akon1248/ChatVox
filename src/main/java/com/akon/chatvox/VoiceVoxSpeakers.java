package com.akon.chatvox;

import com.akon.chatvox.data.Speaker;
import com.akon.chatvox.data.SpeakerStyle;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@UtilityClass
public class VoiceVoxSpeakers {

	@Getter
	private List<Speaker> speakers = List.of();
	private final Map<String, Speaker> byName = Maps.newHashMap();
	private final Table<String, String, SpeakerStyle> styles = HashBasedTable.create();
	private final Int2ObjectMap<Pair<Speaker, SpeakerStyle>> byId = new Int2ObjectOpenHashMap<>();

	private synchronized void _updateSpeakers(Speaker[] newSpeakers) {
		speakers = Collections.unmodifiableList(Arrays.asList(newSpeakers));
		byName.clear();
		styles.clear();
		for (var speaker : newSpeakers) {
			byName.put(speaker.name(), speaker);
			for (var style : speaker.styles()) {
				styles.put(speaker.name(), style.name(), style);
				byId.put(style.id(), Pair.of(speaker, style));
			}
		}
	}

	public void updateSpeakers() {
		ChatVox.LOGGER.info("Fetching speakers");
		VoiceVoxClient.fetchSpeakers().thenAccept(VoiceVoxSpeakers::_updateSpeakers);
	}

	public synchronized @Nullable Speaker getSpeaker(String name) {
		return byName.get(name);
	}

	public synchronized @Nullable SpeakerStyle getStyle(String speaker, String style) {
		return styles.get(speaker, style);
	}

	public synchronized @Nullable Pair<Speaker, SpeakerStyle> getById(int id) {
		return byId.get(id);
	}
}
