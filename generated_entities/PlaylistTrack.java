package com.gcu.chinook.generated;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

/** Generated from table playlist_track. */
@Entity
@Table(name = "playlist_track")
class PlaylistTrack {

    @Id
    @NotNull
    @Column(name = "playlist_id", nullable = false)
    private Integer playlistId;

    @Id
    @NotNull
    @Column(name = "track_id", nullable = false)
    private Integer trackId;

    public Integer getPlaylistId() {
        return playlistId;
    }

    public void setPlaylistId(Integer playlistId) {
        this.playlistId = playlistId;
    }

    public Integer getTrackId() {
        return trackId;
    }

    public void setTrackId(Integer trackId) {
        this.trackId = trackId;
    }
}
