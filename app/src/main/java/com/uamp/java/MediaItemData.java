package com.uamp.java;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;


public class MediaItemData {
    public static final Integer PLAYBACK_RES_CHANGED = 1;
    public static final DiffUtil.ItemCallback<MediaItemData> diffCallback = new DiffUtil.ItemCallback<MediaItemData>() {
        @Override
        public boolean areItemsTheSame(@NonNull @NotNull MediaItemData mediaItemData, @NonNull @NotNull MediaItemData t1) {
            return Objects.equals(mediaItemData.mediaId, t1.mediaId);
        }

        @Override
        public boolean areContentsTheSame(@NonNull @NotNull MediaItemData mediaItemData, @NonNull @NotNull MediaItemData t1) {
            return Objects.equals(mediaItemData.mediaId, t1.mediaId) && Objects.equals(mediaItemData.playbackRes, t1.playbackRes);
        }

        @Nullable
        @Override
        public @org.jetbrains.annotations.Nullable Object getChangePayload(@NonNull @NotNull MediaItemData oldItem, @NonNull @NotNull MediaItemData newItem) {
            if (!Objects.equals(oldItem.playbackRes, newItem.playbackRes)) {
                return PLAYBACK_RES_CHANGED;
            } else {
                return null;
            }
        }
    };
    public final String mediaId;
    public final String title;
    public final String subtitle;
    public final Uri albumArtUri;
    public final Boolean browable;
    public final Integer playbackRes;

    public MediaItemData(String mediaId, String title, String subtitle, Uri albumArtUri, Boolean browable, Integer playbackRes) {
        this.mediaId = mediaId;
        this.title = title;
        this.subtitle = subtitle;
        this.albumArtUri = albumArtUri;
        this.browable = browable;
        this.playbackRes = playbackRes;
    }
}
