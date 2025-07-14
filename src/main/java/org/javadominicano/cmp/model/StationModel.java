package org.javadominicano.cmp.model;

import java.util.List;

public class StationModel {
    private int stationId;
    private String stationModel;
    private List<SensorModel> sensores;

    public List<SensorModel> getSensores() {
        return sensores;
    }

    public void setSensores(List<SensorModel> sensores) {
        this.sensores = sensores;
    }


    // Getters y Setters
    public int getStationId() { return stationId; }
    public void setStationId(int stationId) { this.stationId = stationId; }

    public String getStationModel() { return stationModel; }
    public void setStationModel(String stationModel) { this.stationModel = stationModel; }
}
