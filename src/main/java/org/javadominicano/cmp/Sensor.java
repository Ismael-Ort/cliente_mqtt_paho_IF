package org.javadominicano.cmp;

import java.util.Date;

public class Sensor {
    private String sensorId;
    private String sensorType;
    private double temperatura;
    private Date fecha;
    private String unidad;

    public Sensor() {}

    public Sensor(String sensorId, String sensorType) {
        this.sensorId = sensorId;
        this.sensorType = sensorType;
        this.temperatura = Math.random() * 20 + 15;
        this.fecha = new Date();
        this.unidad = switch (sensorType.toLowerCase()) {
            case "temperatura" -> "\u00B0C";
            case "humedad" -> "%";
            case "viento" -> "km/h";
            default -> "";
        };
    }

    // Getters y Setters
    public String getSensorId() { return sensorId; }
    public void setSensorId(String sensorId) { this.sensorId = sensorId; }

    public String getSensorType() { return sensorType; }
    public void setSensorType(String sensorType) { this.sensorType = sensorType; }

    public double getTemperatura() { return temperatura; }
    public void setTemperatura(double temperatura) { this.temperatura = temperatura; }

    public Date getFecha() { return fecha; }
    public void setFecha(Date fecha) { this.fecha = fecha; }

    public String getUnidad() { return unidad; }
    public void setUnidad(String unidad) { this.unidad = unidad; }
}
