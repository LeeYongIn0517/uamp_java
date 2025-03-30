package com.uamp.java;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;


public class MediaItemData {
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

    public static MediaItemData getInstance() {
        final Integer PLAYBACK_RES_CHANGED = 1;
        /**
         * [DiffUtil.ItemCallback] for a [MediaItemData].
         *
         * Since all [MediaItemData]s have a unique ID, it's easiest to check if two
         * items are the same by simply comparing that ID.
         *
         * To check if the contents are the same, we use the same ID, but it may be the
         * case that it's only the play state itself which has changed (from playing to
         * paused, or perhaps a different item is the active item now). In this case
         * we check both the ID and the playback resource.
         *
         * To calculate the payload, we use the simplest method possible:
         * - Since the title, subtitle, and albumArtUri are constant (with respect to mediaId),
         *   there's no reason to check if they've changed. If the mediaId is the same, none of
         *   those properties have changed.
         * - If the playback resource (playbackRes) has changed to reflect the change in playback
         *   state, that's all that needs to be updated. We return [PLAYBACK_RES_CHANGED] as
         *   the payload in this case.
         * - If something else changed, then refresh the full item for simplicity.
         */
        final DiffUtil.ItemCallback<MediaItemData> diffCallback = new DiffUtil.ItemCallback<MediaItemData>() {
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
    }
}
