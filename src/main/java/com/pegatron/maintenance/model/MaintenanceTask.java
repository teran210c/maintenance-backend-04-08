package com.pegatron.maintenance.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class MaintenanceTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "line_id", nullable = false)
    private Line line;

   @Enumerated(EnumType.STRING)
   @Column(name = "type", columnDefinition = "varchar(255)", nullable = true) // Cambia a nullable true temporalmente
   private MaintenanceType type;


    @Column(nullable = false)
    private LocalDate dueDate;

    private LocalDateTime snoozeUntil;

    private LocalDate performedDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MaintenanceStatus status;
}
