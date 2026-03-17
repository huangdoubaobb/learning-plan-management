package com.example.learningplan.entity;

import javax.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "parent_children", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"parent_id", "child_id"})
})
@SQLDelete(sql = "UPDATE parent_children SET is_deleted = 1 WHERE id = ?")
@Where(clause = "is_deleted = 0")
@Getter
@Setter
public class ParentChild extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", nullable = false)
    private User parent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_id", nullable = false)
    private User child;
}
