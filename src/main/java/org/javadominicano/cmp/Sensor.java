package org.javadominicano.cmp;

import java.util.Date;

public class Sensor {

    private String sensorId;
    private int temperatura; // Ensure this field exists
    private String sensorType;
    private Date fecha = new Date();

    public Sensor(String id, String sensorType) {
        setSensorId(id);
        setSensorType(sensorType);
        int numero = (int) (Math.random() * 100) + 1;
        setTemperatura(numero);
    }

    public int getTemperatura() {
        return temperatura;
    }

    public void setTemperatura(int temperatura) {
        this.temperatura = temperatura; 
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public String getSensorId() {
        return sensorId;
    }

    public void setSensorId(String sensorId) {
        this.sensorId = sensorId;
    }

    public String getSensorType() {
        return sensorType;
    }

    public void setSensorType(String sensorType) {
        this.sensorType = sensorType;
    }
}