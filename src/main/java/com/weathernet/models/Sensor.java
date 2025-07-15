package com.weathernet.models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "sensores")
@Data
public class Sensor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre; // ej: "Temperatura", "Humedad"

    private String tipo; // ej: "DHT22", "BME280"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estacion_id")
    private Estacion estacion;
}