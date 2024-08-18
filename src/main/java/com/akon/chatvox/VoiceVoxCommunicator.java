package com.akon.chatvox;

import lombok.experimental.UtilityClass;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@UtilityClass
public class VoiceVoxCommunicator {

	private final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadScheduledExecutor();

	private String getURL() {
		return "http://localhost:" + ChatVox.config.port + "/";
	}


}
