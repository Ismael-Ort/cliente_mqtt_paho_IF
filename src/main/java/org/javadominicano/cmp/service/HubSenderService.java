package org.javadominicano.cmp.service;

import org.javadominicano.cmp.dto.HubRequestDTO;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class HubSenderService {

    private static final String HUB_URL = "https://itt363-hub.smar.com.do/api/";
    private static final String TOKEN = "p7tWxFnpMfPE";

    private final RestTemplate restTemplate = new RestTemplate();

    public void enviarLectura(HubRequestDTO dto) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("SEGURIDAD-TOKEN", TOKEN);

        HttpEntity<HubRequestDTO> request = new HttpEntity<>(dto, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(HUB_URL, request, String.class);
            System.out.println("✅ Enviado al HUB - Estado: " + response.getStatusCode());
            System.out.println("ℹ️ Respuesta: " + response.getBody());
        } catch (Exception e) {
            System.err.println("❌ Error enviando al HUB: " + e.getMessage());
        }
    }
}
