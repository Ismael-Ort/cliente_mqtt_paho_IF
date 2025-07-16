package org.javadominicano.cmp.dto;

import java.util.Date;

public class ReporteResumenDTO {
    private String nombreEstacion;
    private String sensorNombre;
    private String tipoSensor;
    private double valorMax;
    private Date fechaMax;
    private double valorMin;
    private Date fechaMin;

    public String getNombreEstacion() { return nombreEstacion; }
    public void setNombreEstacion(String nombreEstacion) { this.nombreEstacion = nombreEstacion; }

    public String getSensorNombre() { return sensorNombre; }
    public void setSensorNombre(String sensorNombre) { this.sensorNombre = sensorNombre; }

    public String getTipoSensor() { return tipoSensor; }
    public void setTipoSensor(String tipoSensor) { this.tipoSensor = tipoSensor; }

    public double getValorMax() { return valorMax; }
    public void setValorMax(double valorMax) { this.valorMax = valorMax; }

    public Date getFechaMax() { return fechaMax; }
    public void setFechaMax(Date fechaMax) { this.fechaMax = fechaMax; }

    public double getValorMin() { return valorMin; }
    public void setValorMin(double valorMin) { this.valorMin = valorMin; }

    public Date getFechaMin() { return fechaMin; }
    public void setFechaMin(Date fechaMin) { this.fechaMin = fechaMin; }
}
