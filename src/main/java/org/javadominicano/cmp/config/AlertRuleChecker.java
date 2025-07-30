package org.javadominicano.cmp.config;

import org.javadominicano.cmp.DatabaseManager;
import org.javadominicano.cmp.model.AlertRuleModel;
import org.javadominicano.cmp.model.RecordModel;
import org.javadominicano.cmp.model.SensorModel;
import org.javadominicano.cmp.model.StationModel;
import org.javadominicano.cmp.dto.AlertaDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AlertRuleChecker {
    private final DatabaseManager db;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public AlertRuleChecker(SimpMessagingTemplate messagingTemplate, DatabaseManager db) {
        this.messagingTemplate = messagingTemplate;
        this.db = db;
    }

    @Scheduled(fixedDelay = 60000)
    public void checkRules() {
        List<AlertRuleModel> reglas = db.getAllAlertRules();
        for (AlertRuleModel r : reglas) {
            RecordModel last = db.getLastRecord(r.getSensorId());
            if (last == null) continue;
            boolean cumple = "ALTA".equalsIgnoreCase(r.getTipo())
                    ? last.getValue() > r.getUmbral()
                    : last.getValue() < r.getUmbral();
            if (cumple && !r.isActiva()) {
                String msg = "ALTA".equalsIgnoreCase(r.getTipo())
                        ? "Umbral alto superado" : "Umbral bajo superado";
                db.insertAlert(r.getStationId(), r.getSensorId(), last.getValue(), msg);
                db.updateAlertRuleState(r.getRuleId(), true);

                StationModel st = db.getStationById(r.getStationId());
                SensorModel se = db.getSensorById(r.getSensorId());
                AlertaDTO alerta = new AlertaDTO();
                alerta.setId(0);
                alerta.setFecha(new java.util.Date());
                alerta.setNombreEstacion(st != null ? st.getStationModel() : String.valueOf(r.getStationId()));
                alerta.setSensorNombre(se != null ? se.getSensorModel() : String.valueOf(r.getSensorId()));
                alerta.setTipoSensor(se != null ? se.getSensorType() : "");
                alerta.setValor(last.getValue());
                alerta.setMensaje(msg);
                messagingTemplate.convertAndSend("/topic/alertas", alerta);
            } else if (!cumple && r.isActiva()) {
                db.updateAlertRuleState(r.getRuleId(), false);
            }
        }
    }
}
