package com.hunglevi.backend.repository;

import com.hunglevi.backend.entity.Alert;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    Page<Alert> findByStatus(String status, Pageable p);

    Page<Alert> findByStatusAndAlertType(
        String status, String type, Pageable p);

    long countByStatus(String status);

    long countByTimestampBetween(LocalDateTime from, LocalDateTime to);

    long countByStatusAndTimestampBetween(String status, LocalDateTime from, LocalDateTime to);

    @Query("SELECT a.alertType, COUNT(a) FROM Alert a GROUP BY a.alertType")
    List<Object[]> countGroupByAlertType();

    @Query("SELECT a.alertType, COUNT(a) FROM Alert a GROUP BY a.alertType")
    List<Object[]> countGroupByAttackType();

    @Query("SELECT a.severity, COUNT(a) FROM Alert a GROUP BY a.severity")
    List<Object[]> countGroupBySeverity();

    @Query(
        "SELECT a.alertType, COUNT(a) FROM Alert a WHERE a.timestamp BETWEEN :from AND :to GROUP BY a.alertType")
    List<Object[]> countGroupByAttackTypeBetween(
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to);

    @Query(
        "SELECT a.severity, COUNT(a) FROM Alert a WHERE a.timestamp BETWEEN :from AND :to GROUP BY a.severity")
    List<Object[]> countGroupBySeverityBetween(
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to);

    @Query(
        value = "SELECT DATE(timestamp)::text, COUNT(*) FROM alerts "
            + "WHERE timestamp >= :from GROUP BY DATE(timestamp) ORDER BY 1",
        nativeQuery = true)
    List<Object[]> getTimelineSince(@Param("from") LocalDateTime from);

    @Query(
        value = "SELECT DATE(timestamp)::text, COUNT(*) FROM alerts "
            + "WHERE timestamp BETWEEN :from AND :to GROUP BY DATE(timestamp) ORDER BY 1",
        nativeQuery = true)
    List<Object[]> getTimelineBetween(
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to);
}
