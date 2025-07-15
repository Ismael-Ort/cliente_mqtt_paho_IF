package com.weathernet.controllers;

import com.weathernet.models.AlertaRegla;
import com.weathernet.models.Sensor;
import com.weathernet.repositories.AlertaReglaRepository;
import com.weathernet.repositories.SensorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/alertas")
public class AlertaController {

    @Autowired
    private AlertaReglaRepository alertaReglaRepository;

    @Autowired
    private SensorRepository sensorRepository;

    @PostMapping("/reglas")
    public ResponseEntity<?> crearRegla(
            @RequestParam Long sensorId,
            @RequestParam String condicion,
            @RequestParam Double umbral,
            @RequestParam String mensaje) {

        // 1. Validar y buscar el sensor
        Sensor sensor = sensorRepository.findById(sensorId)
                .orElse(null);

        if (sensor == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "El sensor con ID " + sensorId + " no existe."));
        }

        // 2. Crear y configurar la nueva regla
        AlertaRegla nuevaRegla = new AlertaRegla();
        nuevaRegla.setSensor(sensor);
        nuevaRegla.setCondicion(AlertaRegla.CondicionTipo.valueOf(condicion.toUpperCase()));
        nuevaRegla.setUmbral(umbral);
        nuevaRegla.setMensaje(mensaje);

        // 3. Guardar la regla y responder
        alertaReglaRepository.save(nuevaRegla);
        return ResponseEntity.ok().body(Map.of("message", "Regla creada exitosamente"));
    }
}