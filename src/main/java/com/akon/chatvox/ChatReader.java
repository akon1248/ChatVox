package com.akon.chatvox;

import com.akon.chatvox.util.AudioUtil;
import com.akon.chatvox.util.RomajiConverter;
import com.google.common.collect.ImmutableMap;
import lombok.experimental.UtilityClass;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.sound.SoundCategory;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@UtilityClass
public class ChatReader {

	// We don't have to include vowel alphabets and capital letters because vowel alphabets cannot exist in a converted text
	// and VOICEVOX reads alphabets in the kana-style pronunciation.
	private final Map<Character, String> ALPHABET_KANA_PRONUNCIATIONS = ImmutableMap.<Character, String>builder()
		.put('b', "ぶ").put('c', "く").put('d', "どぅ").put('f', "ふ").put('g', "ぐ")
		.put('h', "ふ").put('j', "じ").put('k', "く").put('l', "る").put('m', "む")
		.put('n', "ん").put('p', "ぷ").put('q', "く").put('r', "る").put('s', "す")
		.put('t', "とぅ").put('v', "ヴ").put('w', "う").put('x', "くす").put('y', "い").put('z', "ず")
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
				// Directly passing InputStream from HttpURLConnection.getInputStream to AudioSystem.getAudioInputStream causes an exception
				// because it doesn't support mark/reset.
				if (e == null) {
					try (var audio = AudioSystem.getAudioInputStream(new ByteArrayInputStream(wav))) {
						var mixer = AudioSystem.getMixer(ChatVox.config.mixer);
						try (var clip = (Clip)mixer.getLine(AudioUtil.LINE_INFO)) {
							var latch = new CountDownLatch(1);
							clip.addLineListener(event -> {
								if (event.getType() == LineEvent.Type.STOP) {
									latch.countDown();
								}
							});
							clip.open(audio);
							var volume = MinecraftClient.getInstance().options.getSoundVolume(SoundCategory.VOICE);
							var volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
							volumeControl.setValue((float) Math.log10(volume) * 20);
							clip.start();
							try {
								latch.await();
							} catch (InterruptedException ignored) {}
							clip.stop();
						}
					} catch (Exception ex) {
						e = ex;
					}
				}
				if (e != null) {
					NotificationUtil.errorWithToast("Failed to read chat", "See the log for details", READING_FAILED, e);
				}
			}, EXECUTOR_SERVICE);
	}
}
