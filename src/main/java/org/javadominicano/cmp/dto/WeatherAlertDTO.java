package org.javadominicano.cmp.dto;

import java.time.LocalDateTime;

public class WeatherAlertDTO {
    private int id;
    private String estacion;
    private String sensor;
    private String mensaje;
    private double valor;
    private LocalDateTime fecha;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getEstacion() { return estacion; }
    public void setEstacion(String estacion) { this.estacion = estacion; }

    public String getSensor() { return sensor; }
    public void setSensor(String sensor) { this.sensor = sensor; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public double getValor() { return valor; }
    public void setValor(double valor) { this.valor = valor; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
}
