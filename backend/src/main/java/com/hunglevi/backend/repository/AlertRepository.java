package com.hunglevi.backend.repository;

import com.hunglevi.backend.entity.Alert;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    Page<Alert> findByStatus(String status, Pageable p);

    Page<Alert> findByStatusAndAlertType(
        String status, String type, Pageable p);

    long countByStatus(String status);

    @Query("SELECT a.alertType, COUNT(a) FROM Alert a GROUP BY a.alertType")
    List<Object[]> countGroupByAlertType();

    @Query(
        value = "SELECT DATE(created_at)::text, COUNT(*) FROM alerts "
            + "WHERE created_at >= :from GROUP BY DATE(created_at) ORDER BY 1",
        nativeQuery = true)
    List<Object[]> getTimelineSince(LocalDateTime from);
}
