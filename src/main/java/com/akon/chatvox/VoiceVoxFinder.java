package com.akon.chatvox;

import com.google.common.io.CharStreams;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStreamReader;

public enum VoiceVoxFinder {
	UNSUPPORTED {
		@Override
		public @Nullable String find() {
			return null;
		}
	},
	WIN {
		@Override
		public @Nullable String find() {
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
				ErrorReporter.error("Failed to find VOICEVOX", e);
			}
			return null;
		}
	};

	public abstract @Nullable String find();

	public static VoiceVoxFinder get() {
		String os = System.getProperty("os.name");
		if (os.toLowerCase().contains("win")) {
			return WIN;
		}
		return UNSUPPORTED;
	}
}
