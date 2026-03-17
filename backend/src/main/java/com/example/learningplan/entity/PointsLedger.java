package com.example.learningplan.entity;

import javax.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "points_ledger")
@SQLDelete(sql = "UPDATE points_ledger SET is_deleted = 1 WHERE id = ?")
@Where(clause = "is_deleted = 0")
@Getter
@Setter
public class PointsLedger extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_id", nullable = false)
    private User child;

    @Column(nullable = false)
    private Integer points;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PointsType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PointsRefType refType;

    private Long refId;

    private String note;
}

