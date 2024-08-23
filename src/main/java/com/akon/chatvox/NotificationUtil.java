package com.akon.chatvox;

import com.akon.chatvox.event.SwitchScreen;
import com.akon.chatvox.gui.InteractiveTextWidget;
import com.akon.chatvox.mixin.ScreenAccessor;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@UtilityClass
public class NotificationUtil {

	private final String PREFIX = "[Chat Vox] ";
	private boolean initializing = true;
	private final List<String> initializationErrors = Lists.newArrayList();

	@Getter
	public enum Level {
		INFO(0x00FF00),
		WARN(0xFFFF00),
		ERROR(0xFF0000);

		private final int color;

		Level(int color) {
			this.color = color;
		}

		public org.slf4j.event.Level toSl4j() {
			return switch (this) {
				case INFO -> org.slf4j.event.Level.INFO;
				case WARN -> org.slf4j.event.Level.WARN;
				case ERROR -> org.slf4j.event.Level.ERROR;
			};
		}
	}

	public void init() {
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
					initializationError(true),
					Text.literal("Drag over the message on the bottom left for details")
				);
			}
		});
	}

	public void notify(Level level, String msg, @Nullable String description, @Nullable Throwable throwable) {
		var lines = msg.split("\n|\r\n");
		var sl4jLevel = level.toSl4j();
		for (int i = 0; i < lines.length - 1; i++) {
			ChatVox.LOGGER.atLevel(sl4jLevel).log(lines[i]);
		}
		ChatVox.LOGGER.atLevel(sl4jLevel).setCause(throwable).log(lines[lines.length - 1]);
		if (initializing) {
			return;
		}
		var client = MinecraftClient.getInstance();
		if (client.world == null) {
			return;
		}
		for (String line : lines) {
			var text = Text.literal(PREFIX + line).styled(style -> {
				var colored = style.withColor(level.getColor());
				if (description != null) {
					return colored.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of(description)));
				}
				return colored;
			});
			client.inGameHud.getChatHud().addMessage(text);
		}
	}

	public void notify(Level level, String msg, @Nullable String description) {
		notify(level, msg, description, null);
	}

	public void notifyWithToast(Level level, String msg, SystemToast.Type toastType, @Nullable String description, @Nullable Throwable throwable) {
		notify(level, msg, description, throwable);
		if (initializing) {
			return;
		}
		var client = MinecraftClient.getInstance();
		if (toastType != null) {
			SystemToast.show(
				client.getToastManager(),
				toastType,
				Text.literal(PREFIX + msg).withColor(level.getColor()),
				description == null ? null : Text.literal(description)
			);
		}
	}

	public void notifyWithToast(Level level, String msg, SystemToast.Type toastType, @Nullable String description) {
		notifyWithToast(level, msg, toastType, description, null);
	}

	public void info(String msg, @Nullable String description, @Nullable Throwable throwable) {
		notify(Level.INFO, msg, description, throwable);
	}

	public void info(String msg, @Nullable String description) {
		info(msg, description, null);
	}

	public void infoWithToast(String msg, SystemToast.Type toastType, @Nullable String description, @Nullable Throwable throwable) {
		notifyWithToast(Level.INFO, msg, toastType, description, throwable);
	}

	public void infoWithToast(String msg, @Nullable String description, SystemToast.Type toastType) {
		infoWithToast(msg, toastType, description, null);
	}

	public void warn(String msg, @Nullable String description, @Nullable Throwable throwable) {
		notify(Level.WARN, msg, description, throwable);
	}

	public void warn(String msg, @Nullable String description) {
		warn(msg, description, null);
	}

	public void warnWithToast(String msg, @Nullable String description, SystemToast.Type toastType, @Nullable Throwable throwable) {
		notifyWithToast(Level.WARN, msg, toastType, description, throwable);
	}

	public void warnWithToast(String msg, @Nullable String description, SystemToast.Type toastType) {
		warnWithToast(msg, description, toastType, null);
	}

	public void error(String msg, @Nullable String description, @Nullable Throwable throwable) {
		notify(Level.ERROR, msg, description, throwable);
		if (initializing) {
			initializationErrors.add(msg);
		}
	}

	public void error(String msg, @Nullable String description) {
		error(msg, description, null);
	}

	public void errorWithToast(String msg, @Nullable String description, SystemToast.Type toastType, @Nullable Throwable throwable) {
		notifyWithToast(Level.ERROR, msg, toastType, description, throwable);
	}

	public void errorWithToast(String msg, @Nullable String description, SystemToast.Type toastType) {
		errorWithToast(msg, description, toastType, null);
	}

	private Text initializationError(boolean toast) {
		return Text.literal(PREFIX + initializationErrors.size() + " error(s) occurred during initialization").styled(style -> {
			var colored = style.withColor(0xFF0000);
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
			accessor.invokeAddDrawableChild(new InteractiveTextWidget(0, screen.height - 22, initializationError(false), screen));
		}
	}
}
