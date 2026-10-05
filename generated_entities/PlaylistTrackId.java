package com.gcu.chinook.generated;

import java.io.Serializable;
import java.util.Objects;

/** Composite key for table playlist_track. */
class PlaylistTrackId implements Serializable {

    private Integer playlistId;
    private Integer trackId;

    public PlaylistTrackId() {
    }

    public PlaylistTrackId(Integer playlistId, Integer trackId) {
        this.playlistId = playlistId;
        this.trackId = trackId;
    }

    public Integer getPlaylistId() {
        return playlistId;
    }

    public Integer getTrackId() {
        return trackId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PlaylistTrackId that)) {
            return false;
        }
        return Objects.equals(playlistId, that.playlistId)
                && Objects.equals(trackId, that.trackId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playlistId, trackId);
    }
}
