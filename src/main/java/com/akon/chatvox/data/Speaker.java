package com.akon.chatvox.data;

import com.google.gson.annotations.SerializedName;

import java.util.UUID;

public record Speaker(String name, @SerializedName("speaker_uuid") UUID speakerUUID, SpeakerStyle[] styles) {}
