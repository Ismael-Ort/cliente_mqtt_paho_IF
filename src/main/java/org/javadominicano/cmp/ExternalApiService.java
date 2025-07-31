package org.javadominicano.cmp;

import com.google.gson.Gson;

// Clase que representa una lectura de sensor
import org.javadominicano.cmp.Sensor;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Servicio utilitario para enviar lecturas al HUB externo.
 */
public class ExternalApiService {

    private static final String API_URL = "https://itt363-hub.smar.com.do/api/";
    private static final String TOKEN = "p7tWxFnpMfPE";
    private static final String GROUP = "3";
    private static final String STATION = "1";
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    /**
     * Envía una lectura al HUB.
     *
     * @param date    fecha de la lectura
     * @param data    mapa con el tipo de sensor y su valor
     */
    public void sendReading(Date date, Map<String, Object> data) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("grupo", GROUP);
            payload.put("estacion", STATION);
            payload.put("fecha", FORMATTER.format(date.toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDateTime()));

            // Solo incluir los campos válidos para el HUB: temperatura y humedad
            if (data.containsKey("temperatura")) {
                payload.put("temperatura", data.get("temperatura"));
            }
            if (data.containsKey("humedad")) {
                payload.put("humedad", data.get("humedad"));
            }

            String json = gson.toJson(payload);
            System.out.println("\uD83D\uDCC3 JSON enviado al HUB: " + json);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("SEGURIDAD-TOKEN", TOKEN)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                    .build();

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(r -> {
                        System.out.println("\u2705 Lectura enviada al HUB, status " + r.statusCode());
                        System.out.println("\u2139\uFE0F Respuesta del servidor: " + r.body());
                    })
                    .exceptionally(e -> {
                        System.out.println("\u274C Error enviando al HUB: " + e.getMessage());
                        return null;
                    });

        } catch (Exception e) {
            System.out.println("\u274C Error preparando envío al HUB:");
            e.printStackTrace();
        }
    }

    /**
     * Envia una lectura completa al HUB externo utilizando los datos del objeto Sensor.
     *
     * @param sensor lectura a enviar
     */
    public void enviarLecturaAHubExterno(Sensor sensor) {
        if (sensor == null) {
            return;
        }

        if (sensor.getFecha() == null) {
            System.out.println("\u274C Fecha nula, no se envía al HUB externo");
            return;
        }

        if (sensor.getTemperatura() == null && sensor.getHumedad() == null) {
            System.out.println("\u274C Temperatura y humedad nulas, no se envía al HUB externo");
            return;
        }

        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("grupo", GROUP);

            String estacion = "1";
            String id = sensor.getSensorId();
            if (id != null && id.contains("2")) {
                estacion = "2";
            }
            payload.put("estacion", estacion);

            payload.put(
                    "fecha",
                    FORMATTER.format(sensor.getFecha().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
            );

            if (sensor.getTemperatura() != null) {
                payload.put("temperatura", sensor.getTemperatura());
            }
            if (sensor.getHumedad() != null) {
                payload.put("humedad", sensor.getHumedad());
            }

            String json = gson.toJson(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("SEGURIDAD-TOKEN", TOKEN)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200 || response.statusCode() == 201) {
                System.out.println("\u2705 Envío al HUB externo exitoso (" + response.statusCode() + ")");
            } else {
                System.out.println("\u274C Error enviando al HUB externo: HTTP " + response.statusCode());
            }

        } catch (Exception e) {
            System.out.println("\u274C Error enviando al HUB externo:");
            e.printStackTrace();
        }
    }
}
