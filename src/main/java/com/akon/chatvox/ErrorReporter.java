package com.akon.chatvox;

import com.akon.chatvox.event.SwitchScreen;
import com.akon.chatvox.gui.InteractiveTextWidget;
import com.akon.chatvox.mixin.ScreenAccessor;
import com.google.common.collect.Lists;
import lombok.experimental.UtilityClass;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@UtilityClass
public class ErrorReporter {

	private static boolean initializing = true;
	private static final List<String> initializationErrors = Lists.newArrayList();

	public void init() {
		initializationErrors.add("Error 1");
		initializationErrors.add("Error 2");
		initializationErrors.add("Error 3");
		ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			if (screen instanceof TitleScreen) {
				addTitleScreenMessage(screen);
			}
		});
		SwitchScreen.EVENT.register((oldScreen, newScreen) -> {
			if (oldScreen instanceof TitleScreen) {
				initializationErrors.clear();
			}
		});
		ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
			initializing = false;
			if (!initializationErrors.isEmpty()) {
				SystemToast.show(
					MinecraftClient.getInstance().getToastManager(),
					new SystemToast.Type(10000L),
					errorMsg(true),
					Text.literal("Drag over the message on the bottom left for details")
				);
			}
		});
	}

	public void error(String msg, @Nullable Throwable throwable) {
		var lines = msg.split("\n|\r\n");
		for (int i = 0; i < lines.length - 1; i++) {
			ChatVox.LOGGER.error(lines[i]);
		}
		ChatVox.LOGGER.error(lines[lines.length - 1], throwable);
		if (initializing) {
			initializationErrors.add(msg);
		}
	}

	public void error(String msg) {
		error(msg, null);
	}

	private Text errorMsg(boolean toast) {
		return Text.literal("Chat Vox: " + initializationErrors.size() + " error(s) occurred during initialization").styled(style -> {
			var colored = style.withColor(TextColor.fromRgb(0xFF0000));
			if (toast) {
				return colored;
			} else {
				return colored.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of(String.join("\n\n", initializationErrors))));
			}
		});
	}

	private void addTitleScreenMessage(Screen screen) {
		if (!initializationErrors.isEmpty()) {
			var accessor = (ScreenAccessor)screen;
			accessor.invokeAddDrawableChild(new InteractiveTextWidget(0, screen.height - 22, errorMsg(false), screen));
		}
	}
}
