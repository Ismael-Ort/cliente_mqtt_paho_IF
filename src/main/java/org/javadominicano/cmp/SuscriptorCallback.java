package org.javadominicano.cmp;

import com.google.gson.Gson;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.javadominicano.cmp.dto.AlertaDTO;
import org.javadominicano.cmp.dto.StationStatusDTO;
import org.javadominicano.cmp.model.RecordModel;
import org.javadominicano.cmp.model.SensorModel;
import org.javadominicano.cmp.model.StationModel;

import java.util.Date;
import java.util.Map;
import java.util.HashMap;

public class SuscriptorCallback implements MqttCallback {

    private final DatabaseManager dbSimulado;
    private final DatabaseManager dbFisico;
    private final SimpMessagingTemplate messagingTemplate;
    private final Gson gson = new Gson();

    public SuscriptorCallback(DatabaseManager dbSimulado, DatabaseManager dbFisico,
                              SimpMessagingTemplate messagingTemplate) {
        this.dbSimulado = dbSimulado;
        this.dbFisico = dbFisico;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void connectionLost(Throwable cause) {
        System.out.println("❌ Conexión perdida con MQTT broker.");
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        System.out.println("\n📥 Tópico: " + topic);
        System.out.println("📨 Contenido: " + message);

        try {
            if (topic.contains("BME280") || topic.contains("HW103")) {
                procesarMensajeFisico(topic, message);
            } else {
                // procesarMensajeSimulado(message); // si deseas añadir lógica simulada
            }
        } catch (Exception e) {
            System.out.println("❌ Error procesando mensaje: " + e.getMessage());
        }
    }

    private void procesarMensajeFisico(String topic, MqttMessage message) {
        try {
            double valor = Double.parseDouble(message.toString());
            Date fecha = new Date();

            String sensorType;
            String unit;
            String sensorModel;

            if (topic.endsWith("/temperatura")) {
                sensorType = "temperatura";
                unit = "°C";
                sensorModel = "Sensor_Temp";
            } else if (topic.endsWith("/humedad")) {
                sensorType = "humedad";
                unit = "%";
                sensorModel = "Sensor_Hum";
            } else if (topic.endsWith("/presion") || topic.endsWith("/presión")) {
                sensorType = "presion";
                unit = "hPa";
                sensorModel = "Sensor_Pres";
            } else if (topic.endsWith("/viento")) {
                sensorType = "viento";
                unit = "m/s";
                sensorModel = "Sensor_Viento";
            } else if (topic.endsWith("/precipitacion") || topic.endsWith("/precipitación")) {
                sensorType = "precipitacion";
                unit = "mm";
                sensorModel = "Sensor_Prec";
            } else if (topic.endsWith("/Humedad_suelo")) {
                sensorType = "humedad_suelo";
                unit = "%";
                sensorModel = "Sensor_HumedadSuelo";
            } else {
                System.out.println("⚠️ Tópico físico desconocido: " + topic);
                return;
            }

            /*String[] partes = topic.split("/");
            if (partes.length < 3) {
                System.out.println("⚠️ Tópico mal formado: " + topic);
                return;
            }
            String stationModel = partes[2];  

            int stationId = dbFisico.getOrCreateStation(stationModel);
            int sensorId = dbFisico.getOrCreateSensor(stationId, sensorModel, sensorType, unit);

            dbFisico.insertRecord(sensorId, valor, fecha);*/

            String stationModel = "Estacion_Fisica_1";
            //String stationModel = "Estacion2";
            int stationId = dbFisico.getOrCreateStation(stationModel);
            int sensorId = dbFisico.getOrCreateSensor(stationId, sensorModel, sensorType, unit);

            dbFisico.insertRecord(sensorId, valor, fecha);

            System.out.printf("✅ Registro físico insertado: estación=%s, sensor=%s, valor=%.2f\n",
                    stationModel, sensorModel, valor);

            // 📡 Enviar actualización de estación por WebSocket
            StationStatusDTO dto = buildStationStatus(stationId);
            if (dto != null) {
                messagingTemplate.convertAndSend("/topic/estaciones", dto);
            }

            // 🔔 Evaluar reglas de alerta configuradas
            dbFisico.getAlertRulesBySensor(stationId, sensorId).forEach(regla -> {
                boolean cumple;
                if ("ALTA".equalsIgnoreCase(regla.getTipo())) {
                    cumple = valor >= regla.getUmbral();
                } else {
                    cumple = valor <= regla.getUmbral();
                }

                if (cumple && !regla.isActiva()) {
                    String msg = "ALTA".equalsIgnoreCase(regla.getTipo())
                            ? "Umbral alto superado" : "Umbral bajo alcanzado";
                    dbFisico.insertAlert(stationId, sensorId, valor, msg);
                    AlertaDTO alerta = buildAlertaDTO(stationId, sensorModel, sensorType, valor, msg);
                    messagingTemplate.convertAndSend("/topic/alertas", alerta);
                    dbFisico.updateAlertRuleState(regla.getRuleId(), true);
                } else if (!cumple && regla.isActiva()) {
                    dbFisico.updateAlertRuleState(regla.getRuleId(), false);
                }
            });


            // 📡 Enviar actualización de estación por WebSocket
            StationStatusDTO dto = buildStationStatus(stationId);
            if (dto != null) {
                messagingTemplate.convertAndSend("/topic/estaciones", dto);
            }


            // 🔔 Evaluar reglas de alerta configuradas
            dbFisico.getAlertRulesBySensor(stationId, sensorId).forEach(regla -> {
                boolean cumple;
                if ("ALTA".equalsIgnoreCase(regla.getTipo())) {
                    cumple = valor >= regla.getUmbral();
                } else {
                    cumple = valor <= regla.getUmbral();
                }

                if (cumple && !regla.isActiva()) {
                    String msg = "ALTA".equalsIgnoreCase(regla.getTipo())
                            ? "Umbral alto superado" : "Umbral bajo alcanzado";
                    dbFisico.insertAlert(stationId, sensorId, valor, msg);

                    AlertaDTO alerta = buildAlertaDTO(stationId, sensorModel, sensorType, valor, msg);
                    messagingTemplate.convertAndSend("/topic/alertas", alerta);

                    dbFisico.updateAlertRuleState(regla.getRuleId(), true);
                } else if (!cumple && regla.isActiva()) {
                    dbFisico.updateAlertRuleState(regla.getRuleId(), false);
                }
            });

        } catch (Exception e) {
            System.out.println("❌ Error al procesar mensaje físico:");
            e.printStackTrace();
        }
    }

    private StationStatusDTO buildStationStatus(int stationId) {
        StationModel station = dbFisico.getStationById(stationId);
        if (station == null) return null;
        StationStatusDTO dto = new StationStatusDTO();
        dto.setStationName(station.getStationModel());

        Map<String, Double> data = new HashMap<>();
        Date last = null;
        for (SensorModel s : dbFisico.getSensorsByStation(stationId)) {
            RecordModel r = dbFisico.getLastRecord(s.getSensorId());
            if (r != null) {
                String tipo = s.getSensorType().toLowerCase().trim().replace("ó", "o");
                double val = Math.round(r.getValue() * 10.0) / 10.0;
                switch (tipo) {
                    case "temperatura" -> data.put("temperatura", val);
                    case "humedad" -> data.put("humedad", val);
                    case "presion" -> data.put("presion", val);
                    case "viento" -> data.put("viento", val);
                    case "precipitacion" -> data.put("precipitacion", val);
                    case "humedad_suelo" -> data.put("humedad_suelo", val);
                }
                if (last == null || r.getRecordDatetime().after(last)) {
                    last = r.getRecordDatetime();
                }
            }
        }
        dto.setData(data);
        dto.setLastUpdate(last);
        dto.setStatus((last != null && new Date().getTime() - last.getTime() < 3 * 60 * 1000)
                ? "EN_LINEA" : "DESCONECTADA");
        return dto;
    }

    private AlertaDTO buildAlertaDTO(int stationId, String sensorModel, String sensorType,
                                     double valor, String mensaje) {
        AlertaDTO alerta = new AlertaDTO();
        alerta.setFecha(new Date());
        StationModel est = dbFisico.getStationById(stationId);
        alerta.setNombreEstacion(est != null ? est.getStationModel() : String.valueOf(stationId));
        alerta.setSensorNombre(sensorModel);
        alerta.setTipoSensor(sensorType);
        alerta.setValor(valor);
        alerta.setMensaje(mensaje);
        return alerta;
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // No se usa en suscriptor
    }
}
