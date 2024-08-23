package com.akon.chatvox;

import lombok.experimental.UtilityClass;

import java.io.IOException;

@UtilityClass
public class VoiceVoxLauncher {

	/**
	 * Launches VOICEVOX if it is not already running
	 * If the path is not set in the config, it will try to find it automatically
	 */
	public void launch() {
		if (OSAdapter.get().isVoiceVoxRunning()) {
			ChatVox.LOGGER.info("Detected VOICEVOX already running");
			return;
		}
		var voiceVox = ChatVox.config.voiceVoxPath;
		if (voiceVox.isEmpty()) {
			voiceVox = OSAdapter.get().findVoiceVox();
		}
		if (voiceVox == null) {
			NotificationUtil.error("Could not find VOICEVOX automatically\nPlease make sure it is installed or the correct path is set in the config", null);
			return;
		}
		ChatVox.LOGGER.info("Launching VOICEVOX");
		try {
			var process = new ProcessBuilder(voiceVox).start();
			Runtime.getRuntime().addShutdownHook(new Thread(process::destroy));
		} catch (IOException e) {
			NotificationUtil.error("Failed to start VOICEVOX\nPlease start it manually", null, e);
		}
	}
}
