package org.javadominicano.cmp;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class Suscriptor {

    public static final String BROKER_URL = "tcp://mqtt.eict.ce.pucmm.edu.do:1883";
    private MqttClient client;
    private final SuscriptorCallback suscriptorCallback;

    public Suscriptor(SuscriptorCallback suscriptorCallback) {
        this.suscriptorCallback = suscriptorCallback;
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
            client.setCallback(suscriptorCallback);

            MqttConnectOptions connectOptions = new MqttConnectOptions();
            connectOptions.setAutomaticReconnect(true);
            connectOptions.setCleanSession(false);
            connectOptions.setUserName("itt363-grupo3");
            connectOptions.setPassword("CnFebqnjbq7F".toCharArray());

            client.connect(connectOptions);

            //Suscribirse a los topics de sensores simulados
            for (int i = 1; i <= 4; i++) {
                client.subscribe("/itt363-grupo3/estacion-" + i + "/BME280/#");
                client.subscribe("/itt363-grupo3/estacion-" + i + "/HW103/#");
                client.subscribe("/itt363-grupo3/estacion-" + i + "/Anemometro/#");
                client.subscribe("/itt363-grupo3/estacion-" + i + "/Pluviometro/#");
                client.subscribe("/itt363-grupo3/estacion-" + i + "/Veleta/#");
            }

            //Suscribirse a los topics de sensores físicos
            /*client.subscribe("/itt363-grupo3/estacion-1/BME280/temperatura");
            client.subscribe("/itt363-grupo3/estacion-1/BME280/humedad");
            client.subscribe("/itt363-grupo3/estacion-1/BME280/presión");*/

        } catch (MqttException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
