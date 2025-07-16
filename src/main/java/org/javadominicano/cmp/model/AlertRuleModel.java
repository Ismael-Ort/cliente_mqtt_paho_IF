package org.javadominicano.cmp.model;

public class AlertRuleModel {
    private int ruleId;
    private int stationId;
    private int sensorId;
    private String tipo; // ALTA o BAJA
    private double umbral;
    private boolean activa; // true si la condicion esta activa actualmente

    public int getRuleId() { return ruleId; }
    public void setRuleId(int ruleId) { this.ruleId = ruleId; }

    public int getStationId() { return stationId; }
    public void setStationId(int stationId) { this.stationId = stationId; }

    public int getSensorId() { return sensorId; }
    public void setSensorId(int sensorId) { this.sensorId = sensorId; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public double getUmbral() { return umbral; }
    public void setUmbral(double umbral) { this.umbral = umbral; }

    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { this.activa = activa; }
}
