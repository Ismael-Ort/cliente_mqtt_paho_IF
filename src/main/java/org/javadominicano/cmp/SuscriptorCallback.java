package org.javadominicano.cmp;

import com.google.gson.Gson;
import org.eclipse.paho.client.mqttv3.*;

import java.util.Date;

public class SuscriptorCallback implements MqttCallback {

    private final DatabaseManager dbSimulado;
    private final DatabaseManager dbFisico;
    private final Gson gson = new Gson();

    public SuscriptorCallback(DatabaseManager dbSimulado, DatabaseManager dbFisico) {
        this.dbSimulado = dbSimulado;
        this.dbFisico = dbFisico;
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

            // 🔔 Evaluar alertas
            String mensajeAlerta = null;
            switch (sensorType.toLowerCase()) {
                case "temperatura":
                    if (valor > 40) mensajeAlerta = "Temperatura excesiva";
                    break;
                case "humedad":
                    if (valor > 90) mensajeAlerta = "Humedad alta";
                    break;
                case "presion":
                    if (valor < 950 || valor > 1050) mensajeAlerta = "Presión fuera de rango";
                    break;
                case "viento":
                    if (valor > 25) mensajeAlerta = "Viento peligroso";
                    break;
                case "precipitacion":
                    if (valor > 50) mensajeAlerta = "Precipitación intensa";
                    break;
                case "humedad_suelo":
                    if (valor > 80) mensajeAlerta = "Humedad del suelo elevada";
                    break;
            }

            if (mensajeAlerta != null) {
                dbFisico.insertAlert(stationId, sensorId, valor, mensajeAlerta);
            }

        } catch (Exception e) {
            System.out.println("❌ Error al procesar mensaje físico:");
            e.printStackTrace();
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // No se usa en suscriptor
    }
}
