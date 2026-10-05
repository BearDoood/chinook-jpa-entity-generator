package com.gcu.chinook.generated;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

/** Generated from table playlist_track. */
@Entity
@Table(name = "playlist_track")
@IdClass(PlaylistTrackId.class)
class PlaylistTrack {

    @Id
    @NotNull
    @Column(name = "playlist_id", nullable = false)
    private Integer playlistId;

    @Id
    @NotNull
    @Column(name = "track_id", nullable = false)
    private Integer trackId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "playlist_id", insertable = false, updatable = false)
    private Playlist playlist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "track_id", insertable = false, updatable = false)
    private Track track;

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

    public Playlist getPlaylist() {
        return playlist;
    }

    public Track getTrack() {
        return track;
    }
}
