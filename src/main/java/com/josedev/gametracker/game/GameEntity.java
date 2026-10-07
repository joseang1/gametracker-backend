package com.josedev.gametracker.game;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "games")
public class GameEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Only set for games imported from RAWG
    @Column(unique = true)
    private Long rawgId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 500)
    private String coverUrl;

    @Lob
    private String description;

    private LocalDate releaseDate;

    // Comma separated values, e.g. "Action, RPG"
    @Column(length = 300)
    private String genres;

    @Column(length = 500)
    private String platforms;

    @Column(length = 200)
    private String developer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private GameSource source;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public GameEntity() {
    }

    private GameEntity(Builder builder) {
        this.id = builder.id;
        this.rawgId = builder.rawgId;
        this.title = builder.title;
        this.coverUrl = builder.coverUrl;
        this.description = builder.description;
        this.releaseDate = builder.releaseDate;
        this.genres = builder.genres;
        this.platforms = builder.platforms;
        this.developer = builder.developer;
        this.source = builder.source;
    }

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        if (source == null)
            source = GameSource.CUSTOM;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRawgId() {
        return rawgId;
    }

    public void setRawgId(Long rawgId) {
        this.rawgId = rawgId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getGenres() {
        return genres;
    }

    public void setGenres(String genres) {
        this.genres = genres;
    }

    public String getPlatforms() {
        return platforms;
    }

    public void setPlatforms(String platforms) {
        this.platforms = platforms;
    }

    public String getDeveloper() {
        return developer;
    }

    public void setDeveloper(String developer) {
        this.developer = developer;
    }

    public GameSource getSource() {
        return source;
    }

    public void setSource(GameSource source) {
        this.source = source;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public static class Builder {
        private Long id;
        private Long rawgId;
        private String title;
        private String coverUrl;
        private String description;
        private LocalDate releaseDate;
        private String genres;
        private String platforms;
        private String developer;
        private GameSource source;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder rawgId(Long rawgId) {
            this.rawgId = rawgId;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder coverUrl(String coverUrl) {
            this.coverUrl = coverUrl;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder releaseDate(LocalDate releaseDate) {
            this.releaseDate = releaseDate;
            return this;
        }

        public Builder genres(String genres) {
            this.genres = genres;
            return this;
        }

        public Builder platforms(String platforms) {
            this.platforms = platforms;
            return this;
        }

        public Builder developer(String developer) {
            this.developer = developer;
            return this;
        }

        public Builder source(GameSource source) {
            this.source = source;
            return this;
        }

        public GameEntity build() {
            return new GameEntity(this);
        }
    }

}
