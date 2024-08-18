package com.akon.chatvox.mixin;

import com.akon.chatvox.event.SwitchScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

	@Shadow
	@Nullable
	public Screen currentScreen;

	@Inject(method = "setScreen", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, target = "Lnet/minecraft/client/MinecraftClient;currentScreen:Lnet/minecraft/client/gui/screen/Screen;"))
	public void onChangeScreen(Screen screen, CallbackInfo ci) {
		SwitchScreen.EVENT.invoker().onSwitchScreen(this.currentScreen, screen);
	}
}
