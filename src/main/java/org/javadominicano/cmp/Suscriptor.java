package org.javadominicano.cmp;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

public class Suscriptor {

    public static final String BROKER_URL = "tcp://mqtt.eict.ce.pucmm.edu.do:1883";
    private MqttClient client;

    public Suscriptor() {
        String clientId = "suscriptor-1";
        try {
            client = new MqttClient(BROKER_URL, clientId);
        } catch (MqttException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void start() {
        try {
            client.setCallback(new SuscriptorCallback());
            MqttConnectOptions connectOptions = new MqttConnectOptions();
            connectOptions.setAutomaticReconnect(true);
            connectOptions.setCleanSession(false);
            connectOptions.setUserName("itt363-grupo3");
            connectOptions.setPassword("CnFebqnjbq7F".toCharArray());

            client.connect(connectOptions);
            for (int i = 1; i <= 4; i++) {
                client.subscribe("/itt363-grupo3/estacion-" + i + "/sensores/#");
            }
        } catch (MqttException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
