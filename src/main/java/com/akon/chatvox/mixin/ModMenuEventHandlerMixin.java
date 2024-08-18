package com.akon.chatvox.mixin;

import com.akon.chatvox.gui.InteractiveTextWidget;
import com.terraformersmc.modmenu.event.ModMenuEventHandler;
import net.minecraft.client.gui.widget.PressableTextWidget;
import net.minecraft.client.gui.widget.Widget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModMenuEventHandler.class)
public class ModMenuEventHandlerMixin {

	@Inject(method = "shiftButtons", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/Widget;setY(I)V", ordinal = 1), cancellable = true)
	private static void preventShiftButtons(Widget widget, boolean shiftUp, int spacing, CallbackInfo ci) {
		if (widget instanceof PressableTextWidget || widget instanceof InteractiveTextWidget) {
			ci.cancel();
		}
	}
}
