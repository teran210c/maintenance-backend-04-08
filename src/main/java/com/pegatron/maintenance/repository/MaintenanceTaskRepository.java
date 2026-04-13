package com.pegatron.maintenance.repository;

import com.pegatron.maintenance.model.MaintenanceTask;
import com.pegatron.maintenance.model.MaintenanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MaintenanceTaskRepository extends JpaRepository<MaintenanceTask, Long> {

    Optional<MaintenanceTask> findByLineIdAndStatus(Long lineId, MaintenanceStatus status);

    // Cambia esto en MaintenanceTaskRepository.java
    @Query("""
    SELECT DISTINCT m.performedDate
    FROM MaintenanceTask m
    WHERE m.line.id = :lineId
    AND m.status = 'COMPLETED'
    AND m.performedDate IS NOT NULL
""")
    List<LocalDate> findCompletedMaintenanceDates(@Param("lineId") Long lineId);

    @Query("""
    SELECT DISTINCT m.performedDate
    FROM MaintenanceTask m
    WHERE m.line.id = :lineId
    AND m.status = 'IN_PROGRESS'
    AND m.performedDate IS NOT NULL
""")
    List<LocalDate> findInProgressMaintenanceDates(@Param("lineId") Long lineId);



    @Query("""
        SELECT DISTINCT 
            COALESCE(CAST(m.snoozeUntil AS date), m.dueDate)
        FROM MaintenanceTask m
        WHERE m.line.id = :lineId
        AND m.status = 'PENDING'
    """)
    List<LocalDate> findPendingMaintenanceDates(@Param("lineId") Long lineId);

    @Query("""
        SELECT m
        FROM MaintenanceTask m
        WHERE m.line.id = :lineId
        AND (m.status = 'PENDING' OR m.status = 'IN_PROGRESS')
        ORDER BY m.id DESC
    """)
    List<MaintenanceTask> findActiveTasks(@Param("lineId") Long lineId);

    @Query("""
        SELECT m
        FROM MaintenanceTask m
        WHERE m.status = 'PENDING'
        AND COALESCE(CAST(m.snoozeUntil AS date), m.dueDate) BETWEEN :today AND :limitDate
        ORDER BY COALESCE(CAST(m.snoozeUntil AS date), m.dueDate) ASC
    """)
    List<MaintenanceTask> findUpcomingMaintenances(@Param("today") LocalDate today, @Param("limitDate") LocalDate limitDate);

    @Query("SELECT COUNT(m) > 0 FROM MaintenanceTask m WHERE m.line.id = :lineId AND m.status != 'COMPLETED'")
    boolean existsActiveByLineId(@Param("lineId") Long lineId);

    // Reemplaza estos métodos en tu MaintenanceTaskRepository.java
    @Query("SELECT m FROM MaintenanceTask m WHERE m.line.id = :lineId AND m.status = 'COMPLETED'")
    List<MaintenanceTask> findCompletedTasks(@Param("lineId") Long lineId);

    @Query("SELECT m FROM MaintenanceTask m WHERE m.line.id = :lineId AND m.status = 'PENDING'")
    List<MaintenanceTask> findPendingTasks(@Param("lineId") Long lineId);

    @Query("SELECT m FROM MaintenanceTask m WHERE m.line.id = :lineId AND m.status = 'IN_PROGRESS'")
    List<MaintenanceTask> findInProgressTasks(@Param("lineId") Long lineId);

}
