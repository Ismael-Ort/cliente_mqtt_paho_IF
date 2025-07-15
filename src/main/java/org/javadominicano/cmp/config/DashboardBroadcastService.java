package org.javadominicano.cmp.config;

import org.javadominicano.cmp.EstacionController;
import org.javadominicano.cmp.dto.AlertaDTO;
import org.javadominicano.cmp.dto.StationStatusDTO;

import org.springframework.beans.factory.annotation.Value;


import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@EnableScheduling
public class DashboardBroadcastService {

    private final SimpMessagingTemplate messagingTemplate;
    private final EstacionController estacionController;

    private final long rateMs;

    public DashboardBroadcastService(SimpMessagingTemplate messagingTemplate,
                                     EstacionController estacionController,
                                     @Value("${broadcast.fixed-rate-ms:1000}") long rateMs) {
        this.messagingTemplate = messagingTemplate;
        this.estacionController = estacionController;
        this.rateMs = rateMs;
    }

    @Scheduled(fixedRateString = "${broadcast.fixed-rate-ms:1000}")


    public DashboardBroadcastService(SimpMessagingTemplate messagingTemplate,
                                     EstacionController estacionController) {
        this.messagingTemplate = messagingTemplate;
        this.estacionController = estacionController;
    }

    @Scheduled(fixedRate = 1000)

    public void sendUpdates() {
        List<StationStatusDTO> status = estacionController.getStationStatusSummary();
        messagingTemplate.convertAndSend("/topic/status", status);

        List<AlertaDTO> alertas = estacionController.obtenerAlertas();
        messagingTemplate.convertAndSend("/topic/alertas", alertas);
    }
}
