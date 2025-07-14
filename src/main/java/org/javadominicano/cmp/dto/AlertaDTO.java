package org.javadominicano.cmp.dto;

import java.util.Date;

public class AlertaDTO {
    private Date fecha;
    private String nombreEstacion;
    private String sensorNombre;
    private String tipoSensor;
    private double valor;
    private String mensaje;

    // Getters y Setters
    public Date getFecha() { return fecha; }
    public void setFecha(Date fecha) { this.fecha = fecha; }

    public String getNombreEstacion() { return nombreEstacion; }
    public void setNombreEstacion(String nombreEstacion) { this.nombreEstacion = nombreEstacion; }

    public String getSensorNombre() { return sensorNombre; }
    public void setSensorNombre(String sensorNombre) { this.sensorNombre = sensorNombre; }

    public String getTipoSensor() { return tipoSensor; }
    public void setTipoSensor(String tipoSensor) { this.tipoSensor = tipoSensor; }

    public double getValor() { return valor; }
    public void setValor(double valor) { this.valor = valor; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
}
