package com.akon.chatvox;

import com.akon.chatvox.util.RomajiConverter;
import com.google.common.collect.ImmutableMap;
import lombok.experimental.UtilityClass;
import net.minecraft.client.toast.SystemToast;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@UtilityClass
public class ChatReader {

	// We don't have to include vowel alphabets and capital letters because vowel alphabets cannot exist in a converted text
	// and VOICEVOX reads alphabets in the kana-style pronunciation.
	private final Map<Character, String> ALPHABET_KANA_PRONUNCIATIONS = ImmutableMap.<Character, String>builder()
		.put('b', "ブ").put('c', "ク").put('d', "ドゥ").put('f', "フ").put('g', "グ")
		.put('h', "フ").put('j', "ジ").put('k', "ク").put('l', "ル").put('m', "ム")
		.put('n', "ン").put('p', "プ").put('q', "ク").put('r', "ル").put('s', "ス")
		.put('t', "トゥ").put('v', "ヴ").put('w', "ウ").put('x', "クス").put('y', "イ").put('z', "ズ")
		.build();

	private final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();

	private final SystemToast.Type READING_FAILED = new SystemToast.Type(10000L);

	public void read(String chatText) {
		if (!ChatVox.isEnabled()) {
			return;
		}
		var text = RomajiConverter.convertToKana(chatText);
		var sb = new StringBuilder();
		for (var i = 0; i < text.length(); i++) {
			var c = text.charAt(i);
			sb.append(ALPHABET_KANA_PRONUNCIATIONS.getOrDefault(c, String.valueOf(c)));
		}
		VoiceVoxClient.synthesize(sb.toString(), ChatVox.config.speaker)
			.whenCompleteAsync((wav, e) -> {
				if (e == null) {
					try (var audio = AudioSystem.getAudioInputStream(new ByteArrayInputStream(wav));
						 var clip = AudioSystem.getClip()) {
						var latch = new CountDownLatch(1);
						clip.addLineListener(event -> {
							if (event.getType() == LineEvent.Type.STOP) {
								latch.countDown();
							}
						});
						clip.open(audio);
						clip.start();
						try {
							latch.await();
						} catch (InterruptedException ignored) {}
						clip.stop();
					} catch (UnsupportedAudioFileException | IOException | LineUnavailableException ex) {
						e = ex;
					}
				}
				if (e != null) {
					NotificationUtil.errorWithToast("Failed to read chat", "See the log for details", READING_FAILED, e);
				}
			}, EXECUTOR_SERVICE);
	}
}
