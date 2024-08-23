package com.akon.chatvox;

import com.google.common.io.CharStreams;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStreamReader;

/**
 * OSAdapter is a utility class that provides platform-specific methods.
 */
public enum OSAdapter {
	UNSUPPORTED {

		private static <T> T unsupported() {
			throw new UnsupportedOperationException("Unsupported OS");
		}

		@Override
		public String findVoiceVox() {
			return unsupported();
		}

		@Override
		public boolean isVoiceVoxRunning() {
			return unsupported();
		}
	},
	WIN {

		@Override
		public @Nullable String findVoiceVox() {
			try {
				var lnkPath = System.getProperty("user.home") + "\\AppData\\Roaming\\Microsoft\\Windows\\Start Menu\\Programs\\VOICEVOX.lnk";
				var shell = new ProcessBuilder("powershell", "(New-Object -ComObject WScript.Shell).CreateShortCut('" + lnkPath + "').TargetPath").start();
				var in = shell.getInputStream();
				var writer = new StringBuilder();
				CharStreams.copy(new InputStreamReader(in), writer);
				if (shell.exitValue() == 0) {
					return writer.toString().trim();
				}
			} catch (IOException e) {
				NotificationUtil.error("Failed to find VOICEVOX", null, e);
			}
			return null;
		}

		private static final String ENGINE_RELATIVE_PATH = "\\vv-engine\\run.exe";
		private static final String CORE_RELATIVE_PATH = "\\VOICEVOX.exe";

		@Override
		public boolean isVoiceVoxRunning() {
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
	};

	public abstract @Nullable String findVoiceVox();

	public abstract boolean isVoiceVoxRunning();

	public static OSAdapter get() {
		var os = System.getProperty("os.name");
		if (os.toLowerCase().contains("win")) {
			return WIN;
		}
		return UNSUPPORTED;
	}
}
