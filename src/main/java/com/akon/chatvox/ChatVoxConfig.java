package com.akon.chatvox;

import com.akon.chatvox.util.AudioUtil;

import javax.sound.sampled.Mixer;

public class ChatVoxConfig {

	public boolean enabled = true;
	public int port = ChatVox.DEFAULT_PORT;
	public String voiceVoxPath = "";
	public int speaker = 3;
	public Mixer.Info mixer = AudioUtil.getDefaultOutputDevice();
}
