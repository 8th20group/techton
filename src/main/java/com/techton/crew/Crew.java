package com.techton.crew;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import com.techton.global.BusinessException;
import jakarta.persistence.Version;
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

    @Version
    private Long version;

    public Crew(String githubId, String nickname, Track track) {
        this.githubId = githubId;
        this.nickname = nickname;
        this.generation = DEFAULT_GENERATION;
        this.track = track;
        this.point = INITIAL_POINT;
    }

    public void earnPoint(int amount) {
        if (amount < 0) {
            throw new BusinessException("적립 포인트는 0 이상이어야 합니다.");
        }
        this.point += amount;
    }

    public void usePoint(int amount) {
        if (amount < 0) {
            throw new BusinessException("사용 포인트는 0 이상이어야 합니다.");
        }
        if (this.point < amount) {
            throw new BusinessException("포인트가 부족합니다.");
        }
        this.point -= amount;
    }
}
