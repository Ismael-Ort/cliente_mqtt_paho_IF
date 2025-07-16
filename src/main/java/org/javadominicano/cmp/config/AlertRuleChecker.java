package org.javadominicano.cmp.config;

import org.javadominicano.cmp.DatabaseManager;
import org.javadominicano.cmp.model.AlertRuleModel;
import org.javadominicano.cmp.model.RecordModel;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AlertRuleChecker {
    private final DatabaseManager db = new DatabaseManager(
            "jdbc:mysql://192.168.100.168/MqttBase",
            "usermqtt",
            "Mqtt1234!"
    );

    @Scheduled(fixedDelay = 60000)
    public void checkRules() {
        List<AlertRuleModel> reglas = db.getAllAlertRules();
        for (AlertRuleModel r : reglas) {
            RecordModel last = db.getLastRecord(r.getSensorId());
            if (last == null) continue;
            boolean cumple = "ALTA".equalsIgnoreCase(r.getTipo())
                    ? last.getValue() >= r.getUmbral()
                    : last.getValue() <= r.getUmbral();
            if (cumple && !r.isActiva()) {
                db.insertAlert(r.getStationId(), r.getSensorId(), last.getValue(),
                        "Umbral " + r.getTipo().toLowerCase());
                db.updateAlertRuleState(r.getRuleId(), true);
            } else if (!cumple && r.isActiva()) {
                db.updateAlertRuleState(r.getRuleId(), false);
            }
        }
    }
}
