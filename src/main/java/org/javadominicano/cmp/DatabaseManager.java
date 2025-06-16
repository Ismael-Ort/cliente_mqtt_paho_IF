package org.javadominicano.cmp;

import java.sql.*;
import java.util.Date;

public class DatabaseManager {

    private static final String URL = "jdbc:mysql://192.168.100.168:3306/estacionees_m";
    private static final String USER = "mqttuser";
    private static final String PASSWORD = "Mqtt1234!";
    /*private static final String URL = "jdbc:mysql://localhost/estacionees_m";
    private static final String USER = "root";
    private static final String PASSWORD = "IF10131014"; */

    private Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("No se encontró el driver de MySQL", e);
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public int getOrCreateMeasurementType(String name, String unit) {
        String select = "SELECT type_id FROM Measurement_Type WHERE name = ?";
        String insert = "INSERT INTO Measurement_Type (name, unit) VALUES (?, ?)";
        try (Connection conn = getConnection()) {
            PreparedStatement sel = conn.prepareStatement(select);
            sel.setString(1, name);
            ResultSet rs = sel.executeQuery();
            if (rs.next()) return rs.getInt("type_id");

            PreparedStatement ins = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS);
            ins.setString(1, name);
            ins.setString(2, unit);
            ins.executeUpdate();
            rs = ins.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int insertStation(String stationModel) {
        String sql = "INSERT INTO Station (station_model, creation_date, location, latitude, longitude) VALUES (?, CURDATE(), ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, stationModel);
            stmt.setString(2, "Ciudad X");
            stmt.setDouble(3, 19.451);
            stmt.setDouble(4, -70.698);
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int insertSensor(int stationId, String sensorModel, String sensorType, String unit) {
        int measurementTypeId = getOrCreateMeasurementType(sensorType, unit);
        String sql = "INSERT INTO Sensor (station_id, sensor_model, sensor_type, measurement_type_id, unit, creation_date) VALUES (?, ?, ?, ?, ?, CURDATE())";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, stationId);
            stmt.setString(2, sensorModel);
            stmt.setString(3, sensorType);
            stmt.setInt(4, measurementTypeId);
            stmt.setString(5, unit);
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void insertRecord(int sensorId, double value, Date recordDateTime) {
        String sql = "INSERT INTO Record (sensor_id, value, record_datetime) VALUES (?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sensorId);
            stmt.setDouble(2, value);
            stmt.setTimestamp(3, new java.sql.Timestamp(recordDateTime.getTime()));
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertSensorStatus(int sensorId, String status, Date statusDate, String notes) {
        String sql = "INSERT INTO Sensor_Status (sensor_id, status, status_date, notes) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sensorId);
            stmt.setString(2, status);
            stmt.setDate(3, new java.sql.Date(statusDate.getTime()));
            stmt.setString(4, notes);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertWeatherAlert(int stationId, String alertType, String description, Date alertDateTime, String severity) {
        String sql = "INSERT INTO Weather_Alert (station_id, alert_type, description, alert_datetime, severity) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, stationId);
            stmt.setString(2, alertType);
            stmt.setString(3, description);
            stmt.setTimestamp(4, new java.sql.Timestamp(alertDateTime.getTime()));
            stmt.setString(5, severity);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
