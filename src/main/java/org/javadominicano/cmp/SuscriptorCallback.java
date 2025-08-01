package org.javadominicano.cmp;

import com.google.gson.Gson;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.javadominicano.cmp.dto.AlertaDTO;
import org.javadominicano.cmp.dto.StationStatusDTO;
import org.javadominicano.cmp.model.RecordModel;
import org.javadominicano.cmp.model.SensorModel;
import org.javadominicano.cmp.model.StationModel;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.javadominicano.cmp.SensorDataAcumulado;
import org.javadominicano.cmp.ApiClient;

public class SuscriptorCallback implements MqttCallback {

    private final DatabaseManager dbSimulado;
    private final DatabaseManager dbFisico;
    private final SimpMessagingTemplate messagingTemplate;
    private final Gson gson = new Gson();
    private final Map<String, SensorDataAcumulado> datosAcumulados = new ConcurrentHashMap<>();

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
            if (topic.contains("BME280") || topic.contains("HW103") ||
                topic.contains("Anemometro") || topic.contains("Pluviometro") ||
                topic.contains("Veleta")
            ) {
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
            } else if (topic.endsWith("/viento") || topic.endsWith("/velocidad_mps")) {
                valor = Double.parseDouble(message.toString());
                sensorType = "viento";
                unit = "m/s";
                sensorModel = "Sensor_Viento";
            } else if (topic.endsWith("/precipitacion") || topic.endsWith("/precipitación") || topic.endsWith("/lluvia")) {
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
            int stationId = dbFisico.getOrCreateStation(stationModel);
            int sensorId = dbFisico.getOrCreateSensor(stationId, sensorModel, sensorType, unit);

            dbFisico.insertRecord(sensorId, valor, fecha);

            actualizarDatosHub(topic, sensorType, message.toString(), valor, fecha);

            System.out.printf("✅ Registro físico insertado: estación=%s, sensor=%s, valor=%.2f\n",
                    stationModel, sensorModel, valor);

            // 📡 Enviar actualización de estación por WebSocket
            StationStatusDTO dto = buildStationStatus(stationId);
            if (dto != null) {
                messagingTemplate.convertAndSend("/topic/estaciones", dto);
            }

            // 🔔 Evaluar reglas de alerta configuradas
            dbFisico.getAlertRulesBySensor(stationId, sensorId).forEach(regla -> {
                try {
                    String tipo = regla.getTipo().toUpperCase();
                    boolean cumple;

                    if (!"ALTA".equals(tipo) && !"BAJA".equals(tipo)) {
                        System.out.printf("⚠️ Tipo de regla desconocido: %s (Regla ID=%d)\n", tipo, regla.getRuleId());
                        return;
                    }

                    cumple = "ALTA".equals(tipo)
                            ? valor > regla.getUmbral()
                            : valor < regla.getUmbral();

                    if (cumple && !regla.isActiva()) {
                        String msg = "ALTA".equals(tipo)
                                ? "Umbral alto superado"
                                : "Umbral bajo superado";

                        dbFisico.insertAlert(stationId, sensorId, valor, msg);
                        dbFisico.updateAlertRuleState(regla.getRuleId(), true);

                        AlertaDTO alerta = buildAlertaDTO(stationId, sensorModel, sensorType, valor, msg);
                        messagingTemplate.convertAndSend("/topic/alertas", alerta);

                        System.out.printf("🚨 Alerta activada: %s | Valor: %.2f | Regla ID: %d\n",
                                msg, valor, regla.getRuleId());
                    } else if (!cumple && regla.isActiva()) {
                        dbFisico.updateAlertRuleState(regla.getRuleId(), false);
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

        StationModel est = dbFisico.getStationById(stationId);
        alerta.setNombreEstacion(est != null ? est.getStationModel() : String.valueOf(stationId));
        alerta.setSensorNombre(sensorModel);
        alerta.setTipoSensor(sensorType);
        alerta.setValor(valor);
        alerta.setMensaje(mensaje);

        return alerta;
    }

    private void actualizarDatosHub(String topic, String sensorType, String valorStr, double valor, Date fecha) {
        String[] partes = topic.split("/");
        if (partes.length < 3) return;

        String estacionId = partes[2];

        SensorDataAcumulado data = datosAcumulados.computeIfAbsent(estacionId, k -> new SensorDataAcumulado());
        data.setFecha(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(fecha));
        data.setEstacionId(estacionId);

        switch (sensorType) {
            case "temperatura" -> { data.setTemperatura(valor); data.sensor_temperatura = partes[3]; }
            case "humedad" -> { data.setHumedad(valor); data.sensor_humedad = partes[3]; }
            case "presion" -> { data.setPresion(valor); data.sensor_presion = partes[3]; }
            case "viento" -> { data.setVelocidad(valor); data.sensor_velocidad = partes[3]; }
            case "direccion_viento" -> { data.setDireccion(valorStr); data.sensor_direccion = partes[3]; }
            case "precipitacion" -> { data.setPrecipitacion(valor); data.sensor_precipitacion = partes[3]; }
            case "humedad_suelo" -> { data.setHumedadSuelo(valor); data.sensor_humedad_suelo = partes[3]; }
        }

        if (data.estaCompleto()) {
            ApiClient.enviarDatos(data.toJsonApi());
            data.reiniciar();
        }
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