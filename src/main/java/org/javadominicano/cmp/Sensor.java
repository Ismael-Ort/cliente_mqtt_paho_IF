package org.javadominicano.cmp;

import java.util.Date;

public class Sensor {
    private String sensorId;
    private String sensorType;
    private Double temperatura;
    private Double humedad;
    private Date fecha;
    private String unidad;

    public Sensor() {}

    public Sensor(String sensorId, String sensorType) {
        this.sensorId = sensorId;
        this.sensorType = sensorType;
        this.fecha = new Date();

        switch (sensorType.toLowerCase()) {
            case "temperatura" -> this.temperatura = Math.random() * 20 + 15;
            case "humedad" -> this.humedad = Math.random() * 40 + 40;
            default -> this.temperatura = Math.random() * 20 + 15;
        }

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

    public Double getTemperatura() { return temperatura; }
    public void setTemperatura(Double temperatura) { this.temperatura = temperatura; }

    public Double getHumedad() { return humedad; }
    public void setHumedad(Double humedad) { this.humedad = humedad; }

    public Date getFecha() { return fecha; }
    public void setFecha(Date fecha) { this.fecha = fecha; }

    public String getUnidad() { return unidad; }
    public void setUnidad(String unidad) { this.unidad = unidad; }
}
