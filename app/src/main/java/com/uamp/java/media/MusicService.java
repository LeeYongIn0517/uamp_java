package com.uamp.java.media;

import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media.MediaBrowserServiceCompat;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MusicService extends MediaBrowserServiceCompat {
    public static final String NETWORK_FAILURE = "com.example.android.uamp.media.session.NETWORK_FAILURE";

    @Nullable
    @Override
    public @org.jetbrains.annotations.Nullable BrowserRoot onGetRoot(@NonNull @NotNull String s, int i, @Nullable @org.jetbrains.annotations.Nullable Bundle bundle) {
        return null;
    }

    @Override
    public void onLoadChildren(@NonNull @NotNull String s, @NonNull @NotNull MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>> result) {

    }
}
