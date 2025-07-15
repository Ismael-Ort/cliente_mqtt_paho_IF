package com.weathernet.models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "alerta_reglas")
@Data
public class AlertaRegla {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sensor_id", nullable = false)
    private Sensor sensor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CondicionTipo condicion;

    @Column(nullable = false)
    private Double umbral;

    @Column(nullable = false)
    private String mensaje;

    public enum CondicionTipo {
        MAYOR_QUE,
        MENOR_QUE
    }
}