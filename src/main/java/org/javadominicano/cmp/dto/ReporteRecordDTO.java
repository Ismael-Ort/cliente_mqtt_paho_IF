package org.javadominicano.cmp.dto;

import java.util.Date;

public class ReporteRecordDTO {
    private String nombreEstacion;
    private String modeloSensor;
    private String tipoSensor;
    private double valor;
    private String unidad;
    private Date fecha;

    // Getters y Setters
    public String getNombreEstacion() { return nombreEstacion; }
    public void setNombreEstacion(String nombreEstacion) { this.nombreEstacion = nombreEstacion; }

    public String getModeloSensor() { return modeloSensor; }
    public void setModeloSensor(String modeloSensor) { this.modeloSensor = modeloSensor; }

    public String getTipoSensor() { return tipoSensor; }
    public void setTipoSensor(String tipoSensor) { this.tipoSensor = tipoSensor; }

    public double getValor() { return valor; }
    public void setValor(double valor) { this.valor = valor; }

    public String getUnidad() { return unidad; }
    public void setUnidad(String unidad) { this.unidad = unidad; }

    public Date getFecha() { return fecha; }
    public void setFecha(Date fecha) { this.fecha = fecha; }
}