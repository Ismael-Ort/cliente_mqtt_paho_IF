package org.javadominicano.cmp.dto;


public class AlertRuleDTO {
    private int ruleId;
    private int stationId;
    private String nombreEstacion;
    private int sensorId;
    private String sensorNombre;
    private String tipo;
    private double umbral;
    private boolean activa;
    private java.util.Date fechaCreacion;

    public int getRuleId() {return ruleId;}
    public void setRuleId(int ruleId) {this.ruleId = ruleId;}

    public int getStationId() {return stationId;}
    public void setStationId(int stationId) {this.stationId = stationId;}

    public String getNombreEstacion() {return nombreEstacion;}
    public void setNombreEstacion(String nombreEstacion) {this.nombreEstacion = nombreEstacion;}

    public int getSensorId() {return sensorId;}
    public void setSensorId(int sensorId) {this.sensorId = sensorId;}

    public String getSensorNombre() {return sensorNombre;}
    public void setSensorNombre(String sensorNombre) {this.sensorNombre = sensorNombre;}

    public String getTipo() {return tipo;}
    public void setTipo(String tipo) {this.tipo = tipo;}

    public double getUmbral() {return umbral;}
    public void setUmbral(double umbral) {this.umbral = umbral;}

    public boolean isActiva() {return activa;}
    public void setActiva(boolean activa) {this.activa = activa;}

    public java.util.Date getFechaCreacion() {return fechaCreacion;}
    public void setFechaCreacion(java.util.Date fechaCreacion) {this.fechaCreacion = fechaCreacion;}
}
