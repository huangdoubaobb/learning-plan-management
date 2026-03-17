package com.example.learningplan.entity;

import javax.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "redemptions")
@SQLDelete(sql = "UPDATE redemptions SET is_deleted = 1 WHERE id = ?")
@Where(clause = "is_deleted = 0")
@Getter
@Setter
public class Redemption extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reward_id", nullable = false)
    private Reward reward;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_id", nullable = false)
    private User child;

    @Column(nullable = false)
    private Integer pointsCost;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RedemptionStatus status = RedemptionStatus.PENDING;

    @Column(nullable = false)
    private LocalDateTime redeemedAt;

    @Column(length = 500)
    private String reviewNote;

    private LocalDateTime reviewedAt;

    @PrePersist
    public void prePersist() {
        if (redeemedAt == null) {
            redeemedAt = LocalDateTime.now();
        }
    }
}

