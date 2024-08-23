package com.akon.chatvox;

import com.akon.chatvox.data.Speaker;
import com.google.common.io.ByteStreams;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Unit;
import lombok.experimental.UtilityClass;
import net.minecraft.client.toast.SystemToast;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.*;

@UtilityClass
public class VoiceVoxClient {

	private final ScheduledExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadScheduledExecutor();
	private final SystemToast.Type CONNECTION_STATUS = new SystemToast.Type(10000L);

	private boolean isConnected = false;

	public void init() {
		EXECUTOR_SERVICE.submit(() -> {
			var result = checkAlive();
			result.left().ifPresentOrElse(
				e -> NotificationUtil.errorWithToast("Failed to connect to VOICEVOX engine", null, CONNECTION_STATUS, e.orElse(null)),
				() -> {
					isConnected = true;
					NotificationUtil.infoWithToast("Connection to VOICEVOX engine established", null, CONNECTION_STATUS);
					VoiceVoxSpeakers.updateSpeakers();
				}
			);
		});
		EXECUTOR_SERVICE.scheduleAtFixedRate(() -> {
			if (isConnected != checkAlive().right().isPresent()) {
				isConnected = !isConnected;
				if (!isConnected) {
					NotificationUtil.errorWithToast("Connection to VOICEVOX engine lost", "Chat Vox is now disabled until the connection is restored", CONNECTION_STATUS);
				} else {
					NotificationUtil.infoWithToast("Connection to VOICEVOX engine restored", "Chat Vox is now enabled", CONNECTION_STATUS);
					VoiceVoxSpeakers.updateSpeakers();
				}
			}
		}, 0, 1000, TimeUnit.MILLISECONDS);
	}

	public boolean connected() {
		return isConnected;
	}

	/**
	 * @return URL of the VoiceVox server without a trailing slash
	 */
	private String getURL() {
		return "http://127.0.0.1:" + ChatVox.config.port;
	}

	private <T> CompletableFuture<T> wrap(Callable<T> task) {
		var future = new CompletableFuture<T>();
		EXECUTOR_SERVICE.submit(() -> {
			try {
				future.complete(task.call());
			} catch (Exception e) {
				future.completeExceptionally(e);
			}
		});
		return future;
	}

	private Either<Optional<IOException>, Unit> checkAlive() {
		try {
			var connection = (HttpURLConnection)URI.create(getURL()).toURL().openConnection();
			connection.setRequestMethod("GET");
			int code = connection.getResponseCode();
			if (code == 200) {
				return Either.right(Unit.INSTANCE);
			} else {
				return Either.left(Optional.empty());
			}
		} catch (IOException e) {
			return Either.left(Optional.of(e));
		}
	}

	private byte[] _synthesize(String text, int speakerId) throws IOException {
		var encoded = URLEncoder.encode(text, StandardCharsets.UTF_8);
		var audioQueryUrl = URI.create(getURL() + "/audio_query?text=" + encoded + "&speaker=" + speakerId).toURL();
		var audioQueryCon = (HttpURLConnection)audioQueryUrl.openConnection();
		audioQueryCon.setRequestMethod("POST");
		try (var in = audioQueryCon.getInputStream()) {
			var synthesisUrl = URI.create(getURL() + "/synthesis?speaker=" + speakerId + "&enable_interrogative_upspeak=true").toURL();
			var synthesisCon = (HttpURLConnection)synthesisUrl.openConnection();
			synthesisCon.setRequestMethod("POST");
			synthesisCon.setRequestProperty("Content-Type", "application/json");
			synthesisCon.setDoOutput(true);
			try (var out = synthesisCon.getOutputStream()) {
				ByteStreams.copy(in, out);
				out.flush();
			}
			try (var wav = synthesisCon.getInputStream()) {
				return ByteStreams.toByteArray(wav);
			}
		}
	}

	private Speaker[] _fetchSpeakers() throws IOException, JsonSyntaxException, JsonIOException {
		try (var in = URI.create(getURL() + "/speakers").toURL().openStream()) {
			return ChatVox.GSON.fromJson(new InputStreamReader(in), Speaker[].class);
		}
	}

	public CompletableFuture<byte[]> synthesize(String text, int speakerId) {
		return wrap(() -> _synthesize(text, speakerId));
	}

	public CompletableFuture<Speaker[]> fetchSpeakers() {
		return wrap(VoiceVoxClient::_fetchSpeakers);
	}
}
