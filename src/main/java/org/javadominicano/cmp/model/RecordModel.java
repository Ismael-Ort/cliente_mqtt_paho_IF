package org.javadominicano.cmp.model;

import java.util.Date;

public class RecordModel {
    private int recordId;
    private int sensorId;
    private float value;
    private Date recordDatetime;

    // Getters y Setters
    public int getRecordId() { return recordId; }
    public void setRecordId(int recordId) { this.recordId = recordId; }

    public int getSensorId() { return sensorId; }
    public void setSensorId(int sensorId) { this.sensorId = sensorId; }

    public float getValue() { return value; }
    public void setValue(float value) { this.value = value; }

    public Date getRecordDatetime() { return recordDatetime; }
    public void setRecordDatetime(Date recordDatetime) { this.recordDatetime = recordDatetime; }
}
