package org.javadominicano.cmp.config;

import org.javadominicano.cmp.DatabaseManager;
import org.javadominicano.cmp.dto.AlertaDTO;
import org.javadominicano.cmp.model.StationModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;

import java.util.Collections;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class StationMonitor {
    private final DatabaseManager db;
    private final SimpMessagingTemplate messagingTemplate;

    private final Set<Integer> offlineStations =
            Collections.synchronizedSet(new HashSet<>());


    @Autowired
    public StationMonitor(SimpMessagingTemplate messagingTemplate, DatabaseManager db) {
        this.messagingTemplate = messagingTemplate;
        this.db = db;
    }

    @Scheduled(fixedDelay = 5000)
    public void checkStations() {
        List<StationModel> estaciones = db.getStations();
        Date now = new Date();
        for (StationModel est : estaciones) {
            Date last = db.getLastRecordTimeByStation(est.getStationId());
            boolean desconectada = (last == null || now.getTime() - last.getTime() > 10000);

            if (desconectada) {
                if (!offlineStations.contains(est.getStationId())) {
                    offlineStations.add(est.getStationId());
                    if (!db.hasDisconnectAlert(est.getStationId())) {
                        db.insertAlert(est.getStationId(), 0, 0.0,
                                "Estación desconectada por inactividad");

                        AlertaDTO alerta = new AlertaDTO();
                        alerta.setId(0);
                        alerta.setFecha(new Date());
                        alerta.setNombreEstacion(est.getStationModel());
                        alerta.setSensorNombre("N/A");
                        alerta.setTipoSensor("N/A");
                        alerta.setValor(0.0);
                        alerta.setMensaje("Estación desconectada por inactividad");

                        messagingTemplate.convertAndSend("/topic/alertas", alerta);
                    }
                }
            } else {
                offlineStations.remove(est.getStationId());
            }
        }
    }
}
