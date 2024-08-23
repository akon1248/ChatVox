package com.akon.chatvox;

import com.akon.chatvox.util.RomajiConverter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ChatVoxTest {

	@Test
	public void romajiTest() {
		assertEquals("", RomajiConverter.convertToKana(""));
		assertEquals("ア", RomajiConverter.convertToKana("a"));
		assertEquals("b", RomajiConverter.convertToKana("b"));
		assertEquals("カ", RomajiConverter.convertToKana("ka"));
		assertEquals("コレハテストデス", RomajiConverter.convertToKana("korehatesutodesu"));
		assertEquals("マインクラフト", RomajiConverter.convertToKana("mainkurafuto"));
		assertEquals("モッド", RomajiConverter.convertToKana("moddo"));
		assertEquals("Mイネcラft", RomajiConverter.convertToKana("Minecraft"));
	}
}
