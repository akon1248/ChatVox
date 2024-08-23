package com.akon.chatvox;

import com.akon.chatvox.util.RomajiConverter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ChatVoxTest {

	@Test
	public void romajiTest() {
		assertEquals("", RomajiConverter.convertToKana(""));
		assertEquals("あ", RomajiConverter.convertToKana("a"));
		assertEquals("b", RomajiConverter.convertToKana("b"));
		assertEquals("か", RomajiConverter.convertToKana("ka"));
		assertEquals("これはてすとです", RomajiConverter.convertToKana("korehatesutodesu"));
		assertEquals("まいんくらふと", RomajiConverter.convertToKana("mainkurafuto"));
		assertEquals("もっど", RomajiConverter.convertToKana("moddo"));
		assertEquals("すうぇーでん", RomajiConverter.convertToKana("suwe-denn"));
		assertEquals("Mいねcらft", RomajiConverter.convertToKana("Minecraft"));
	}
}
