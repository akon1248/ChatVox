package com.akon.chatvox;

import lombok.experimental.UtilityClass;

import java.io.IOException;

@UtilityClass
public class VoiceVoxLauncher {

	public void launch() {
		if (isAlreadyRunning()) {
			ChatVox.LOGGER.info("Detected VOICEVOX already running");
			return;
		}
		String voiceVox = ChatVox.config.voiceVoxPath;
		if (voiceVox.isEmpty()) {
			voiceVox = VoiceVoxFinder.get().find();
		}
		if (voiceVox == null) {
			ErrorReporter.error("Could not find VOICEVOX automatically\nPlease make sure it is installed or the correct path is set in the config");
			return;
		}
		ChatVox.LOGGER.info("Launching VOICEVOX");
		try {
			var process = new ProcessBuilder(voiceVox).start();
			Runtime.getRuntime().addShutdownHook(new Thread(process::destroy));
		} catch (IOException e) {
			ErrorReporter.error("Failed to start VOICEVOX\nPlease start it manually", e);
		}
	}

	private static final String ENGINE_RELATIVE_PATH = "\\vv-engine\\run.exe";
	private static final String CORE_RELATIVE_PATH = "\\VOICEVOX.exe";

	private boolean isAlreadyRunning() {
		return ProcessHandle.allProcesses()
			.anyMatch(proc ->
				proc.info().command().filter(cmd -> {
					if (!cmd.endsWith(ENGINE_RELATIVE_PATH)) {
						return false;
					}
					var parent = proc.parent();
					if (parent.isEmpty()) {
						return false;
					}
					return parent
						.flatMap(p -> p.info().command())
						.filter(c -> c.equals(cmd.substring(0, cmd.length() - ENGINE_RELATIVE_PATH.length()) + CORE_RELATIVE_PATH))
						.isPresent();
				}).isPresent()
			);
	}
}
