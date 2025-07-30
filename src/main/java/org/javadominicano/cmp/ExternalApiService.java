package org.javadominicano.cmp;

import com.google.gson.Gson;
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
            payload.putAll(data);

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
}
