package com.akon.chatvox.mixin;

import com.akon.chatvox.ChatReader;
import net.minecraft.client.gui.screen.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ChatScreen.class, priority = 0)
public class ChatScreenMixin {

	@Inject(method = "sendMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendChatMessage(Ljava/lang/String;)V"))
	private void onChat(String chatText, boolean addToHistory, CallbackInfo ci) {
		ChatReader.read(chatText);
	}
}
