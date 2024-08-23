package com.akon.chatvox.util;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Nullable;

import javax.sound.sampled.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@UtilityClass
public class AudioUtil {

	public final DataLine.Info LINE_INFO = new DataLine.Info(
		Clip.class,
		new AudioFormat(
			AudioFormat.Encoding.PCM_SIGNED,
			AudioSystem.NOT_SPECIFIED,
			16,
			2,
			4,
			AudioSystem.NOT_SPECIFIED,
			true
		)
	);

	private Stream<Mixer.Info> getOutputDeviceStream() {
		return Arrays.stream(AudioSystem.getMixerInfo())
			.filter(mixerInfo -> AudioSystem.getMixer(mixerInfo).isLineSupported(LINE_INFO));
	}

	public List<Mixer.Info> getOutputDevices() {
		return getOutputDeviceStream().toList();
	}

	public Mixer.Info getDefaultOutputDevice() {
		return getOutputDeviceStream().findFirst().get();
	}

	@Nullable
	public Mixer.Info getOutputDeviceByName(String name) {
		return getOutputDeviceStream()
			.filter(info -> info.getName().equals(name))
			.findFirst()
			.orElse(null);
	}
}
