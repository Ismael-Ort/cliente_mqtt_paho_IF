package org.javadominicano.cmp;

import com.google.gson.Gson;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.javadominicano.cmp.model.StationModel;
import org.javadominicano.cmp.Sensor;

import java.util.List;

public class Publicador {

    private static final String BROKER_URL = "tcp://mqtt.eict.ce.pucmm.edu.do:1883";
    private MqttClient client;
    private static final DatabaseManager dbManager = new DatabaseManager();

    public Publicador(String id) {
        try {
            client = new MqttClient(BROKER_URL, id);
        } catch (MqttException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void enviarMensaje(String topic, String mensaje, Sensor sensor, int sensorId) {
        try {
            MqttConnectOptions connectOptions = new MqttConnectOptions();
            connectOptions.setAutomaticReconnect(true);
            connectOptions.setCleanSession(false);
            connectOptions.setUserName("itt363-grupo3");
            connectOptions.setPassword("CnFebqnjbq7F".toCharArray());

            client.connect(connectOptions);
            client.publish(topic, mensaje.getBytes(), 2, false);
            client.disconnect();
            client.close();

            dbManager.insertRecord(sensorId, sensor.getTemperatura(), sensor.getFecha());

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public static void iniciarPrueba() {
        List<StationModel> estaciones = dbManager.getStations();
        String[] tipos = {"Temperatura", "Humedad", "Presión"};

        for (StationModel est : estaciones) {
            int stationId = est.getStationId();
            for (String tipo : tipos) {
                String nombreSensor = "Sensor" + tipo.charAt(0) + stationId;
                String idSensor = nombreSensor.toLowerCase();
                String topic = "/itt363-grupo3/estacion-" + stationId + "/sensores/" + tipo.toLowerCase();
                String unidad = unidadDe(tipo);
                int sensorDBId = dbManager.getOrCreateSensor(stationId, nombreSensor, tipo, unidad);
                lanzarHiloSensor(idSensor, tipo, topic, sensorDBId);
            }
        }
    }

    private static void lanzarHiloSensor(String sensorId, String tipo, String topic, int sensorDBId) {
        new Thread(() -> {
            Gson gson = new Gson();
            while (true) {
                Sensor sensor = new Sensor(sensorId, tipo);
                new Publicador(sensorId).enviarMensaje(topic, gson.toJson(sensor), sensor, sensorDBId);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private static String unidadDe(String tipo) {
        return switch (tipo.toLowerCase()) {
            case "temperatura" -> "°C";
            case "humedad"     -> "%";
            case "presión"     -> "hPa";
            default            -> "";
        };
    }
}
