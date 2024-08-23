package com.akon.chatvox.util;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import lombok.experimental.UtilityClass;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@UtilityClass
public class RomajiConverter {

	/**
	 * A table of romaji to kana mappings.
	 * Each entry is a pair of a romaji string and a kana string and represents a syllable or a small kana.
	 */
	private final List<Pair<String, String>> ROMAJI_TABLE;

	static {
		var csv = Objects.requireNonNull(RomajiConverter.class.getResourceAsStream("/romaji.csv"));
		try (var reader = new BufferedReader(new InputStreamReader(csv, StandardCharsets.UTF_8))) {

			var builder = ImmutableList.<Pair<String, String>>builder();
			reader.lines().forEach(line -> {
				var split = line.split(",");
				builder.add(Pair.of(split[0], split[1]));
			});
			ROMAJI_TABLE = builder.build();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private boolean isNonNConsonant(char c) {
		if (c < 'b' || c > 'z') {
			return false;
		}
		return switch (c) {
			case 'e', 'i', 'o', 'u', 'n' -> false;
			default -> true;
		};
	}

	/**
	 * Searches for possible conversions of the first part of the given romaji string.
	 * If the first part is a valid romaji, it returns the corresponding kana,
	 * otherwise the result simply represents whether there is a valid romaji starting with the given string.
	 * If includeSokuon is true, it also considers the case where the first part is a sokuon which starts with twice repeated consonants.
	 *
	 * @param romaji the first part of the romaji string to search for
	 * @param includeSokuon whether to consider the sokuon case
	 * @return a left value if there is no possible conversion,
	 * a right value with an empty optional if there is a possible conversion, but it is not a valid romaji,
	 * or a right value with a non-empty optional if there is a valid romaji
	 */
	private Either<Unit, Optional<String>> searchForPossibleConversions(String romaji, boolean includeSokuon) {
		if (includeSokuon && romaji.length() >= 2 && romaji.charAt(0) == romaji.charAt(1) && isNonNConsonant(romaji.charAt(0))) {
			return searchForPossibleConversions(romaji.substring(1), false)
				.mapRight(opt -> opt.map(kana -> "っ" + kana));
		}
		var left = 0;
		var right = ROMAJI_TABLE.size() - 1;
		var success = false;
		while (left <= right) {
			var mid = left + (right - left) / 2;
			var entry = ROMAJI_TABLE.get(mid);
			var cmp = entry.getFirst().compareTo(romaji);
			if (cmp == 0) {
				return Either.right(Optional.of(entry.getSecond()));
			} else {
				if (entry.getFirst().startsWith(romaji)) {
					success = true;
				}
				if (cmp < 0) {
					left = mid + 1;
				} else {
					right = mid - 1;
				}
			}
		}
		if (success) {
			return Either.right(Optional.empty());
		}
		return Either.left(Unit.INSTANCE);
	}

	public String convertToKana(String romaji) {
		if (romaji.isEmpty()) {
			return "";
		}
		var sb = new StringBuilder();
		int start = 0, end = 1;
		while (start < end || end < romaji.length()) {
			var sub = romaji.substring(start, end);
			var result = searchForPossibleConversions(sub, true);
			if (result.right().isPresent()) {
				var opt = result.right().get();
				if (opt.isPresent()) {
					sb.append(opt.get());
					start = end;
					end = Math.min(end + 1, romaji.length());
					continue;
				} else if (end < romaji.length()) {
					end++;
					continue;
				}
			}

			// Handles the cases of "n" + consonant
			if (sub.length() == 2 && sub.charAt(0) == 'n') {
				// It is unreachable here in the cases such as "na", "nu" and "nn" because they are already converted.
				sb.append("ん");
				start++;
				continue;
			}

			sb.append(sub.charAt(0));
			start++;
		}
		return sb.toString();
	}

}
