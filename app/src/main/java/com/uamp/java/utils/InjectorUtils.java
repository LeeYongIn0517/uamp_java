package com.uamp.java.utils;

import android.content.ComponentName;
import android.content.Context;

import com.uamp.java.common.MusicServiceConnection;
import com.uamp.java.media.MusicService;

public class InjectorUtils {
    private static final InjectorUtils instance = new InjectorUtils();

    // private constructor to avoid client applications to use constructor
    private InjectorUtils() {
    }

    public static InjectorUtils getInstance() {
        return instance;
    }

    private MusicServiceConnection provideMusicServiceConnection(Context context) {
        return MusicServiceConnection.getInstance(
                context, new ComponentName(context, MusicService.class)
        );
    }
}
