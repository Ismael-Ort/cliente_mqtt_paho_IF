package org.javadominicano.cmp;

import com.google.gson.Gson;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

public class Publicador {

    private static final String BROKER_URL = "tcp://mqtt.eict.ce.pucmm.edu.do:1883";
    private MqttClient client;
    private static DatabaseManager dbManager = new DatabaseManager();
    private static int[] stationIds = new int[4];
    private static int[] sensorIds = new int[16]; 

    public Publicador(String id) {
        try {
            client = new MqttClient(BROKER_URL, id);
        } catch (MqttException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void enviarMensaje(String topic, String mensaje, Sensor sensor, int sensorId) {
    System.out.println("Enviando Información Topic: " + topic);
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
        System.exit(1);
    }
}

    public static void iniciarPrueba() {
        for (int i = 0; i < 4; i++) {
            stationIds[i] = dbManager.insertStation("Modelo_E" + (i + 1));
        }

        String[] tipos = {"Temperatura", "Humedad", "Presión", "Viento"};
        int contadorSensor = 0;

        for (int est = 0; est < 4; est++) {
            for (String tipo : tipos) {
                String nombreSensor = "Sensor" + tipo.charAt(0) + (est + 1);
                String idSensor = nombreSensor.toLowerCase();
                String topic = "/itt363-grupo3/estacion-" + (est + 1) + "/sensores/" + tipo.toLowerCase();
                String unidad = unidadDe(tipo);
                sensorIds[contadorSensor] = dbManager.insertSensor(stationIds[est], nombreSensor, tipo, unidad);
                lanzarHiloSensor(idSensor, tipo, topic, sensorIds[contadorSensor]);
                contadorSensor++;
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
            case "viento"      -> "km/h";
            default            -> "";
        };
    }
}
