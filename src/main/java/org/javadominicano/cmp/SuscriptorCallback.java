package org.javadominicano.cmp;

import com.google.gson.Gson;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.javadominicano.cmp.dto.AlertaDTO;
import org.javadominicano.cmp.dto.StationStatusDTO;
import org.javadominicano.cmp.model.RecordModel;
import org.javadominicano.cmp.model.SensorModel;
import org.javadominicano.cmp.model.StationModel;

import java.util.Date;
import java.util.Map;
import java.util.HashMap;

@Component
public class SuscriptorCallback implements MqttCallback {

    private final DatabaseManager dbManager;
    private final SimpMessagingTemplate messagingTemplate;
    private final Gson gson = new Gson();

    public SuscriptorCallback(DatabaseManager dbManager,
                              SimpMessagingTemplate messagingTemplate) {
        this.dbManager = dbManager;
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
            if (topic.contains("BME280") || topic.contains("HW103") || 
                topic.contains("Anemometro") || topic.contains("Pluviometro") || 
                topic.contains("Veleta")) {
                procesarMensajeFisico(topic, message);
            }
        } catch (Exception e) {
            System.out.println("❌ Error procesando mensaje: " + e.getMessage());
        }
    }

    private void procesarMensajeFisico(String topic, MqttMessage message) {
        try {
            double valor;
            Date fecha = new Date();

            String sensorType, unit, sensorModel;

            if (topic.endsWith("/direccion_viento") || topic.endsWith("/direccion")) {
                sensorType = "direccion_viento";
                unit = "";
                sensorModel = "Sensor_DirViento";
                valor = convertirDireccionAFloat(message.toString());
            } else if (topic.endsWith("/temperatura")) {
                valor = Double.parseDouble(message.toString());
                sensorType = "temperatura";
                unit = "°C";
                sensorModel = "Sensor_Temp";
            } else if (topic.endsWith("/humedad")) {
                valor = Double.parseDouble(message.toString());
                sensorType = "humedad";
                unit = "%";
                sensorModel = "Sensor_Hum";
            } else if (topic.endsWith("/presion") || topic.endsWith("/presión")) {
                valor = Double.parseDouble(message.toString());
                sensorType = "presion";
                unit = "hPa";
                sensorModel = "Sensor_Pres";
            } else if (topic.endsWith("/velocidad_mps")) {
                valor = Double.parseDouble(message.toString());
                sensorType = "viento";
                unit = "m/s";
                sensorModel = "Sensor_Viento";
            } else if (topic.endsWith("/lluvia")) {
                valor = Double.parseDouble(message.toString());
                sensorType = "precipitacion";
                unit = "mm";
                sensorModel = "Sensor_Prec";
            } else if (topic.endsWith("/Humedad_suelo")) {
                valor = Double.parseDouble(message.toString());
                sensorType = "humedad_suelo";
                unit = "%";
                sensorModel = "Sensor_HumedadSuelo";
            } else {
                System.out.println("⚠️ Tópico físico desconocido: " + topic);
                return;
            }

            String stationModel = "Estacion_Fisica_1";
            int stationId = dbManager.getOrCreateStation(stationModel);
            int sensorId = dbManager.getOrCreateSensor(stationId, sensorModel, sensorType, unit);

            dbManager.insertRecord(sensorId, valor, fecha);

            System.out.printf("✅ Registro físico insertado: estación=%s, sensor=%s, valor=%.2f\n",
                    stationModel, sensorModel, valor);

            // 📡 Enviar actualización de estación por WebSocket
            StationStatusDTO dto = buildStationStatus(stationId);
            if (dto != null) {
                messagingTemplate.convertAndSend("/topic/estaciones", dto);
            }

            // 🔔 Evaluar reglas de alerta configuradas
            dbManager.getAlertRulesBySensor(stationId, sensorId).forEach(regla -> {
                try {
                    String tipo = regla.getTipo().toUpperCase();
                    boolean cumple;

                    if (!"ALTA".equals(tipo) && !"BAJA".equals(tipo)) {
                        System.out.printf("⚠️ Tipo de regla desconocido: %s (Regla ID=%d)\n", tipo, regla.getRuleId());
                        return;
                    }

                    cumple = "ALTA".equals(tipo)
                            ? valor >= regla.getUmbral()
                            : valor <= regla.getUmbral();

                    if (cumple && !regla.isActiva()) {
                        String msg = "ALTA".equals(tipo)
                                ? "Umbral alto superado"
                                : "Umbral bajo alcanzado";

                        dbManager.insertAlert(stationId, sensorId, valor, msg);
                        dbManager.updateAlertRuleState(regla.getRuleId(), true);

                        AlertaDTO alerta = buildAlertaDTO(stationId, sensorModel, sensorType, valor, msg);
                        messagingTemplate.convertAndSend("/topic/alertas", alerta);

                        System.out.printf("🚨 Alerta activada: %s | Valor: %.2f | Regla ID: %d\n",
                                msg, valor, regla.getRuleId());
                    } else if (!cumple && regla.isActiva()) {
                        dbManager.updateAlertRuleState(regla.getRuleId(), false);
                        System.out.printf("✅ Alerta desactivada (Regla ID %d)\n", regla.getRuleId());
                    }

                } catch (Exception e) {
                    System.out.println("❌ Error al evaluar regla de alerta:");
                    e.printStackTrace();
                }
            });

        } catch (Exception e) {
            System.out.println("❌ Error al procesar mensaje físico:");
            e.printStackTrace();
        }
    }


    private StationStatusDTO buildStationStatus(int stationId) {
        StationModel station = dbManager.getStationById(stationId);
        if (station == null) return null;

        StationStatusDTO dto = new StationStatusDTO();
        dto.setStationName(station.getStationModel());

        Map<String, Double> data = new HashMap<>();
        Date last = null;

        for (SensorModel s : dbManager.getSensorsByStation(stationId)) {
            RecordModel r = dbManager.getLastRecord(s.getSensorId());
            if (r != null) {
                String tipo = s.getSensorType().toLowerCase().trim().replace("ó", "o");
                double val = Math.round(r.getValue() * 10.0) / 10.0;
                switch (tipo) {
                    case "temperatura" -> data.put("temperatura", val);
                    case "humedad" -> data.put("humedad", val);
                    case "presion" -> data.put("presion", val);
                    case "viento" -> data.put("viento", val);
                    case "direccion_viento" -> data.put("direccion_viento", val);
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

        StationModel est = dbManager.getStationById(stationId);
        alerta.setNombreEstacion(est != null ? est.getStationModel() : String.valueOf(stationId));
        alerta.setSensorNombre(sensorModel);
        alerta.setTipoSensor(sensorType);
        alerta.setValor(valor);
        alerta.setMensaje(mensaje);

        return alerta;
    }

    private double convertirDireccionAFloat(String direccion) {
        return switch (direccion.trim().toUpperCase()) {
            case "N" -> 0.0;
            case "NE" -> 45.0;
            case "E" -> 90.0;
            case "SE" -> 135.0;
            case "S" -> 180.0;
            case "SW", "SO" -> 225.0;
            case "W", "O" -> 270.0;
            case "NW", "NO" -> 315.0;
            default -> -1.0;
        };
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // No se usa en suscriptor
    }
}