package com.techton.crew;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Crew {

    private static final int DEFAULT_GENERATION = 8;
    private static final int INITIAL_POINT = 0;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String githubId;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    private int generation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Track track;

    @Column(nullable = false)
    private int point;

    public Crew(String githubId, String nickname, Track track) {
        this.githubId = githubId;
        this.nickname = nickname;
        this.generation = DEFAULT_GENERATION;
        this.track = track;
        this.point = INITIAL_POINT;
    }

    public void addPoint(int point) {
        this.point += point;
    }
}
