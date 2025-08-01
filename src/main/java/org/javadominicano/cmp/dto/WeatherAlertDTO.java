package org.javadominicano.cmp.dto;

import java.time.LocalDateTime;

public class WeatherAlertDTO {
    private int id;
    private String estacion;
    private String sensor;

    private String tipoSensor;

    private String tipo;
    private double umbral;
    private LocalDateTime fecha;
    private String estado;

    private String mensaje;
    private double valor;



    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getEstacion() { return estacion; }
    public void setEstacion(String estacion) { this.estacion = estacion; }

    public String getSensor() { return sensor; }
    public void setSensor(String sensor) { this.sensor = sensor; }

    public String getTipoSensor() { return tipoSensor; }
    public void setTipoSensor(String tipoSensor) { this.tipoSensor = tipoSensor; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public double getUmbral() { return umbral; }
    public void setUmbral(double umbral) { this.umbral = umbral; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public double getValor() { return valor; }
    public void setValor(double valor) { this.valor = valor; }

}
