package com.pegatron.maintenance.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class MaintenanceModule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "maintenance_id", nullable = false)
    private MaintenanceTask maintenance;

    @ManyToOne
    @JoinColumn(name = "line_module_id", nullable = false)
    private LineModule lineModule;

    private String moduleName;
}