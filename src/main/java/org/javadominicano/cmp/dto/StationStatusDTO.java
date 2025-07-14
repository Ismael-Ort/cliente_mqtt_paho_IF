package org.javadominicano.cmp.dto;

import java.util.Date;
import java.util.Map;

public class StationStatusDTO {
    private String stationName;
    private String status; // EN_LINEA o DESCONECTADA
    private Date lastUpdate;
    private Map<String, Double> data;

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public Map<String, Double> getData() {
        return data;
    }

    public void setData(Map<String, Double> data) {
        this.data = data;
    }
}
