package com.example.learningplan.repository;

import com.example.learningplan.entity.Task;
import com.example.learningplan.entity.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByChildId(Long childId);
    List<Task> findByParentId(Long parentId);
    List<Task> findByParentIdAndChildId(Long parentId, Long childId);
    List<Task> findByParentIdAndCreatedAtBetween(Long parentId, LocalDateTime start, LocalDateTime end);
    List<Task> findByParentIdAndChildIdAndCreatedAtBetween(Long parentId, Long childId, LocalDateTime start, LocalDateTime end);
    List<Task> findByChildIdAndCreatedAtBetween(Long childId, LocalDateTime start, LocalDateTime end);
    List<Task> findByChildIdAndCompletedAtBetween(Long childId, LocalDateTime start, LocalDateTime end);
    long countByChildId(Long childId);
    long countByChildIdAndStatus(Long childId, TaskStatus status);
    long countByParentId(Long parentId);
    long countByParentIdAndCreatedAtBetween(Long parentId, LocalDateTime start, LocalDateTime end);

    @Query("select t from Task t where t.parent.id = :parentId " +
        "and (:childId is null or t.child.id = :childId) " +
        "and (:status is null or t.status = :status) " +
        "and (:startDate is null or t.createdAt >= :startDate) " +
        "and (:endDate is null or t.createdAt <= :endDate) " +
        "and (:q is null or lower(t.title) like lower(concat('%',:q,'%')) " +
        "or lower(coalesce(t.description,'')) like lower(concat('%',:q,'%')) " +
        "or lower(coalesce(t.checkinNote,'')) like lower(concat('%',:q,'%')))")
    Page<Task> searchTasksByCreatedAt(@Param("parentId") Long parentId, @Param("childId") Long childId,
                                      @Param("status") TaskStatus status, @Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate, @Param("q") String q, Pageable pageable);

    @Query("select t from Task t where t.parent.id = :parentId and t.completedAt is not null " +
        "and (:childId is null or t.child.id = :childId) " +
        "and (:status is null or t.status = :status) " +
        "and (:startDate is null or t.completedAt >= :startDate) " +
        "and (:endDate is null or t.completedAt <= :endDate) " +
        "and (:q is null or lower(t.title) like lower(concat('%',:q,'%')) " +
        "or lower(coalesce(t.description,'')) like lower(concat('%',:q,'%')) " +
        "or lower(coalesce(t.checkinNote,'')) like lower(concat('%',:q,'%')))")
    Page<Task> searchTasksByCompletedAt(@Param("parentId") Long parentId, @Param("childId") Long childId,
                                        @Param("status") TaskStatus status, @Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate, @Param("q") String q, Pageable pageable);

    @Query("select count(t) from Task t where t.parent.id = :parentId " +
        "and (:childId is null or t.child.id = :childId) " +
        "and (:status is null or t.status = :status) " +
        "and (:startDate is null or t.createdAt >= :startDate) " +
        "and (:endDate is null or t.createdAt <= :endDate) " +
        "and (:q is null or lower(t.title) like lower(concat('%',:q,'%')) " +
        "or lower(coalesce(t.description,'')) like lower(concat('%',:q,'%')) " +
        "or lower(coalesce(t.checkinNote,'')) like lower(concat('%',:q,'%')))")
    long countSearchTasksByCreatedAt(@Param("parentId") Long parentId, @Param("childId") Long childId,
                                     @Param("status") TaskStatus status, @Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate, @Param("q") String q);

    @Query("select count(t) from Task t where t.parent.id = :parentId and t.completedAt is not null " +
        "and (:childId is null or t.child.id = :childId) " +
        "and (:status is null or t.status = :status) " +
        "and (:startDate is null or t.completedAt >= :startDate) " +
        "and (:endDate is null or t.completedAt <= :endDate) " +
        "and (:q is null or lower(t.title) like lower(concat('%',:q,'%')) " +
        "or lower(coalesce(t.description,'')) like lower(concat('%',:q,'%')) " +
        "or lower(coalesce(t.checkinNote,'')) like lower(concat('%',:q,'%')))")
    long countSearchTasksByCompletedAt(@Param("parentId") Long parentId, @Param("childId") Long childId,
                                       @Param("status") TaskStatus status, @Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate, @Param("q") String q);
}