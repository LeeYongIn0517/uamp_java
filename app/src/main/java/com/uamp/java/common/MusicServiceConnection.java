package com.uamp.java.common;

import static com.uamp.java.media.MusicService.NETWORK_FAILURE;

import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

/**
 * Class that manages a connection to a [MediaBrowserServiceCompat] instance, typically a
 * [MusicService] or one of its subclasses.
 * <p>
 * Typically it's best to construct/inject dependencies either using DI or, as UAMP does,
 * using [InjectorUtils] in the app module. There are a few difficulties for that here:
 * - [MediaBrowserCompat] is a final class, so mocking it directly is difficult.
 * - A [MediaBrowserConnectionCallback] is a parameter into the construction of
 * a [MediaBrowserCompat], and provides callbacks to this class.
 * - [MediaBrowserCompat.ConnectionCallback.onConnected] is the best place to construct
 * a [MediaControllerCompat] that will be used to control the [MediaSessionCompat].
 * <p>
 * Because of these reasons, rather than constructing additional classes, this is treated as
 * a black box (which is why there's very little logic here).
 * <p>
 * This is also why the parameters to construct a [MusicServiceConnection] are simple
 * parameters, rather than private properties. They're only required to build the
 * [MediaBrowserConnectionCallback] and [MediaBrowserCompat] objects.
 */
public class MusicServiceConnection {
    private static volatile MusicServiceConnection instance;
    @SuppressWarnings("PropertyName")
    final PlaybackStateCompat EMPTY_PLAYBACK_STATE = new PlaybackStateCompat.Builder().setState(PlaybackStateCompat.STATE_ERROR, 0, 0f).build();
    @SuppressWarnings("PropertyName")
    final MediaMetadataCompat NOTHING_PLAYING = new MediaMetadataCompat.Builder().putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, "").putLong(MediaMetadataCompat.METADATA_KEY_DURATION, 0).build();
    final MutableLiveData<Boolean> isConnected = new MutableLiveData<>();
    final MutableLiveData<Boolean> networkFailure = new MutableLiveData<>();
    final String rootMediaId;
    final MutableLiveData<PlaybackStateCompat> playbackState = new MutableLiveData<>();
    final MutableLiveData<MediaMetadataCompat> nowPlaying = new MutableLiveData<>();
    private final MusicServiceConnection.MediaBrowserConnectionCallback mediaBrowserConnectionCallback;
    private final MediaBrowserCompat mediaBroswer;
    MediaControllerCompat.TransportControls transportControls;
    private MediaControllerCompat mediaController;

    public MusicServiceConnection(Context context, ComponentName serviceComponent) {
        mediaBrowserConnectionCallback = new MediaBrowserConnectionCallback(context);
        mediaBroswer = new MediaBrowserCompat(context, serviceComponent, mediaBrowserConnectionCallback, null);
        mediaBroswer.connect();
        rootMediaId = mediaBroswer.getRoot();
        isConnected.postValue(false);
        networkFailure.postValue(false);
    }

    public static MusicServiceConnection getInstance(Context context, ComponentName serviceComponent) {
        if (instance == null) { //Double-Checked Locking 패턴
            synchronized (MusicServiceConnection.class) { //멀티스레드 환경에서도 하나의 인스턴스만 생성되도록 보장
                if (instance == null) { //정말 인스턴스가 없는 경우에만 생성되도록 보장
                    instance = new MusicServiceConnection(context, serviceComponent);
                }
            }
        }
        return instance;
    }

    void subscribe(String parentId, MediaBrowserCompat.SubscriptionCallback callback) {
        mediaBroswer.subscribe(parentId, callback);
    }

    void unsubscribe(String parentId, MediaBrowserCompat.SubscriptionCallback callback) {
        mediaBroswer.unsubscribe(parentId, callback);
    }

    Boolean sendCommand(String command, @Nullable Bundle parameters) {
        return sendCommand(command, parameters, (resultCode, resultData) -> {
        });
    }

    Boolean sendCommand(String command, @Nullable Bundle parameters, ResultCallback resultCallback) {
        if (mediaBroswer.isConnected()) {
            mediaController.sendCommand(command, parameters, new ResultReceiver(new Handler()) {
                @Override
                protected void onReceiveResult(int resultCode, Bundle resultData) {
                    resultCallback.onResult(resultCode, resultData);
                }
            });
            return true;
        } else {
            return false;
        }
    }

    private interface ResultCallback {
        void onResult(int resultCode, @Nullable Bundle resultData);
    }

    private class MediaBrowserConnectionCallback extends MediaBrowserCompat.ConnectionCallback {
        private final Context context;

        private MediaBrowserConnectionCallback(Context context) {
            this.context = context;
        }

        /**
         * Invoked after [MediaBrowserCompat.connect] when the request has successfully
         * completed.
         */
        @Override
        public void onConnected() {
            mediaController = new MediaControllerCompat(context, mediaBroswer.getSessionToken());
            mediaController.registerCallback(new MediaControllerCallback());
            transportControls = mediaController.getTransportControls();

            isConnected.postValue(true);
        }

        /**
         * Invoked when the client is disconnected from the media browser.
         */
        @Override
        public void onConnectionSuspended() {
            isConnected.postValue(false);
        }

        /**
         * Invoked when the connection to the media browser failed.
         */
        @Override
        public void onConnectionFailed() {
            isConnected.postValue(false);
        }
    }

    private class MediaControllerCallback extends MediaControllerCompat.Callback {

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            playbackState.postValue(state == null ? EMPTY_PLAYBACK_STATE : state);
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            // When ExoPlayer stops we will receive a callback with "empty" metadata. This is a
            // metadata object which has been instantiated with default values. The default value
            // for media ID is null so we assume that if this value is null we are not playing
            // anything.
            if (metadata != null) {
                String id = metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);
                nowPlaying.postValue(id == null ? NOTHING_PLAYING : metadata);
            }
        }

        @Override
        public void onQueueChanged(List<MediaSessionCompat.QueueItem> queue) {
            super.onQueueChanged(queue);
        }

        @Override
        public void onSessionEvent(String event, Bundle extras) {
            super.onSessionEvent(event, extras);
            if (event == NETWORK_FAILURE) {
                networkFailure.postValue(true);
            }
        }

        /**
         * Normally if a [MediaBrowserServiceCompat] drops its connection the callback comes via
         * [MediaControllerCompat.Callback] (here). But since other connection status events
         * are sent to [MediaBrowserCompat.ConnectionCallback], we catch the disconnect here and
         * send it on to the other callback.
         */
        @Override
        public void onSessionDestroyed() {
            mediaBrowserConnectionCallback.onConnectionSuspended();
        }
    }
}
