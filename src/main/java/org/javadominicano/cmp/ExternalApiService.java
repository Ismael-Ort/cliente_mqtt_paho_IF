package org.javadominicano.cmp;

import org.javadominicano.cmp.dto.HubRequestDTO;
import org.javadominicano.cmp.service.HubSenderService;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;

/**
 * Servicio utilitario para enviar lecturas al HUB externo.
 */
@Component
public class ExternalApiService {

    private static final String GROUP = "3";
    private static final String STATION = "1";
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final HubSenderService hubSenderService;

    public ExternalApiService(HubSenderService hubSenderService) {
        this.hubSenderService = hubSenderService;
    }

    /**
     * Envía una lectura al HUB.
     *
     * @param date fecha de la lectura
     * @param data mapa con el tipo de sensor y su valor
     */
    public void sendReading(Date date, Map<String, Object> data) {
        try {
            HubRequestDTO dto = new HubRequestDTO();
            dto.setGrupo(GROUP);
            dto.setEstacion(STATION);
            dto.setFecha(FORMATTER.format(date.toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDateTime()));

            // Solo incluir los campos válidos para el HUB: temperatura y humedad
            if (data.containsKey("temperatura")) {
                dto.setTemperatura(Double.valueOf(data.get("temperatura").toString()));
            }
            if (data.containsKey("humedad")) {
                dto.setHumedad(Double.valueOf(data.get("humedad").toString()));
            }

            hubSenderService.enviarLectura(dto);

        } catch (Exception e) {
            System.out.println("❌ Error preparando envío al HUB:");
            e.printStackTrace();
        }
    }
}
