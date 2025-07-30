package org.javadominicano.cmp.dto;

import java.time.LocalDateTime;

public class HubRequestDTO {
    private String grupo;
    private String estacion;
    private String fecha;
    private Double temperatura;
    private Double humedad;

    // Getters y setters
    public String getGrupo() { return grupo; }
    public void setGrupo(String grupo) { this.grupo = grupo; }

    public String getEstacion() { return estacion; }
    public void setEstacion(String estacion) { this.estacion = estacion; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public Double getTemperatura() { return temperatura; }
    public void setTemperatura(Double temperatura) { this.temperatura = temperatura; }

    public Double getHumedad() { return humedad; }
    public void setHumedad(Double humedad) { this.humedad = humedad; }
}
