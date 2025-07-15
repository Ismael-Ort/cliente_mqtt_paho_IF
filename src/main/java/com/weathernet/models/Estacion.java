package com.weathernet.models;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "estaciones")
@Data
public class Estacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre;

    @OneToMany(mappedBy = "estacion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Sensor> sensores = new ArrayList<>();

}