package com.akon.chatvox.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.gui.screen.Screen;

public interface SwitchScreen {
	Event<SwitchScreen> EVENT = EventFactory.createArrayBacked(SwitchScreen.class, listeners -> (oldScreen, newScreen) -> {
		for (SwitchScreen listener : listeners) {
			listener.onSwitchScreen(oldScreen, newScreen);
		}
	});

	void onSwitchScreen(Screen oldScreen, Screen newScreen);
}
